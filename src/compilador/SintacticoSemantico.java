/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: ___________    HORA: ___________ HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un metodo por cada simbolo
 *:                 No-Terminal de la gramatica mas el metodo emparejar ().
 *:                 El analisis empieza invocando al metodo del simbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 22/Feb/2015 FGil                -Se mejoro errorEmparejar () para mostrar el
 *:                                 numero de linea en el codigo fuente donde 
 *:                                 ocurrio el error.
 *: 08/Sep/2015 FGil                -Se dejo lista para iniciar un nuevo analizador
 *:                                 sintactico.
 *: 06/11/2018  FCaldera            -Se agregaron las constantes "VACIO" y "ERROR_TIPO", 
 *:                                  así como el método "tiposCompatibles", "buscaTipo"
 *:                                  y checarArchivo.
 *:-----------------------------------------------------------------------------
 */
package compilador;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class SintacticoSemantico {

    private Compilador cmp;
    private boolean analizarSemantica = false;
    private String preAnalisis;
    public static final String VACIO = "vacio";
    public static final String ERROR_TIPO = "error_tipo";

    public boolean tiposCompatibles(String tipo1, String tipo2) {
        return false;
    }

    public String buscaTipo(int entrada) {
        return cmp.ts.buscaTipo(entrada);
    }

    /*----------------------------------------------------------------------------------------*/
    // Metodo para comprobar la existencia en disco del archivo "nomarchivo" 
    // y en su caso cargar su contenido en la Tabla de Simbolos.
    // El argumento representa el nombre de un archivo de texto con extension
    //  ".db" que contiene el esquema (dise�o) de una tabla de base de datos. 
    // Los archivos .db tienen el siguiente dise�o:
    //     Dato            ColIni    ColFin
    //     ==================================
    //     nombre-columna  1         25
    //     tipo-de-dato    30        40
    //
    // Ejemplo:  alumnos.db
    //          1         2         3        
    // 1234567890123456789012345678901234567890
    // ==========================================
    // numctrl                      char(8)  
    // nombre                       char(25)
    // edad                         int
    // promedio                     float
    //
    // Cada columna se carga en la Tabla de Simbolos con Complex = "id" y
    // Tipo = "columna(t)"  siendo t  el tipo de dato de la columna.
    // ----------------------------------------------------------------------
    // 20/Oct/2018: Si en la T.S. ya existe la columna con el mismo ambito 
    // que el que se va a registrar solo se sustituye el TIPO si est� en blanco.
    // Si existe la columna pero no tiene ambito entonces se rellenan los datos
    // del tipo y el ambito. 
    private boolean checarArchivo(String nomarchivo) {
        FileReader fr = null;
        BufferedReader br = null;
        String linea = null;
        String columna = null;
        String tipo = null;
        String ambito = null;
        boolean existeArch = false;
        int pos;

        try {
            // Intentar abrir el archivo con el dise�o de la tabla  
            fr = new FileReader(nomarchivo);
            cmp.ts.anadeTipo(cmp.be.preAnalisis.getEntrada(), "tabla");
            br = new BufferedReader(fr);

            // Leer linea x linea, cada linea es la especificacion de una columna
            linea = br.readLine();
            while (linea != null) {
                // Extraer nombre y tipo de dato de la columna
                try {
                    columna = linea.substring(0, 24).trim();
                } catch (Exception err) {
                    columna = "ERROR";
                }
                try {
                    tipo = linea.substring(29).trim();
                } catch (Exception err) {
                    tipo = "ERROR";
                }
                try {
                    ambito = nomarchivo.substring(0, nomarchivo.length() - 3);
                } catch (Exception err) {
                    ambito = "ERROR";
                }
                // Agregar a la tabla de simbolos
                Linea_TS lts = new Linea_TS("id",
                        columna,
                        "COLUMNA(" + tipo + ")",
                        ambito
                );
                // Checar si en la Tabla de Simbolos existe la entrada para un 
                // lexema y ambito iguales al de columna y ambito de la tabla .db
                if ((pos = cmp.ts.buscar(columna, ambito)) > 0) {
                    // YA EXISTE: Si no tiene tipo asignarle el tipo columna(t) 
                    if (cmp.ts.buscaTipo(pos).trim().isEmpty()) {
                        cmp.ts.anadeTipo(pos, tipo);
                    }
                } else {
                    // NO EXISTE: Buscar si en la T. de S. existe solo el lexema de la columna
                    if ((pos = cmp.ts.buscar(columna)) > 0) {
                        // SI EXISTE: checar si el ambito esta en blanco
                        Linea_TS aux = cmp.ts.obt_elemento(pos);
                        if (aux.getAmbito().trim().isEmpty()) {
                            // Ambito en blanco rellenar el tipo y el ambito  
                            cmp.ts.anadeTipo(pos, "COLUMNA(" + tipo + ")");
                            cmp.ts.anadeAmbito(pos, ambito);

                        } else {
                            // Insertar un nuevo elemento a la tabla de simb.
                            cmp.ts.insertar(lts);
                        }
                    } else {
                        // NO EXISTE: insertar un nuevo elemento a la tabla de simb.
                        cmp.ts.insertar(lts);
                    }
                }

                // Leer siguiente linea
                linea = br.readLine();
            }
            existeArch = true;
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            // Cierra los streams de texto si es que se crearon
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException ex) {
            }
        }
        return existeArch;
    }

    /*----------------------------------------------------------------------------------------*/
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //
    public SintacticoSemantico(Compilador c) {
        cmp = c;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metodo que inicia la ejecucion del analisis sintactico predictivo.
    // analizarSemantica : true = realiza el analisis semantico a la par del sintactico
    //                     false= realiza solo el analisis sintactico sin comprobacion semantica
    public void analizar(boolean analizarSemantica) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *
        PROGRAMASQL();
    }

    //--------------------------------------------------------------------------
    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;
        } else {
            errorEmparejar(t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
    private void errorEmparejar(String _token, String _lexema, int numLinea) {
        String msjError = "";

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasig")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + (_lexema.equals("$") ? "fin de archivo" : _lexema)
                + ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico
    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }

    // Fin de error
    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------
//Autor: Cabrales Coronado Heber - 13130684
    private void PROGRAMASQL(Atributos PROGRAMASQL) {

        Atributos DECLARACION = new Atributos();
        Atributos SENTENCIAS = new Atributos();

        if (preAnalisis.equals("declare") || preAnalisis.equals("if")
                || preAnalisis.equals("while") || preAnalisis.equals("print")
                || preAnalisis.equals("select") || preAnalisis.equals("delete")
                || preAnalisis.equals("insert") || preAnalisis.equals("update")
                || preAnalisis.equals("create") || preAnalisis.equals("drop")
                || preAnalisis.equals("assign") || preAnalisis.equals("case")
                || preAnalisis.equals("end")) {
            DECLARACION(DECLARACION);
            SENTENCIAS(SENTENCIAS);
            emparejar("end");

            if(DECLARACION.tipo.equals(VACIO) && SENTENCIAS.tipo.equals(VACIO)){
                PROGRAMASQL.tipo = VACIO;
            }
            else{
                PROGRAMASQL.tipo = ERROR_TIPO;
            }

        } else {
            error("[PROGRAMASQL] inicio no correcto" + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//Autor: Cabrales Coronado Heber - 13130684
    private void ACTREGS(Atributos ACTREGS) {

        Atributos IGUALACION = new Atributos();
        Atributos EXPCOND = new Atributos();
        Linea_BE id = new Linea_BE();

        if (preAnalisis.equals("update")) {
            emparejar("update");
            id = cmp.be.preAnalisis;
            emparejar("id");
            emparejar("set");
            IGUALACION(IGUALACION);

            if(checarArchivo(id.lexema || ".db")){
                EXPCOND.ambito = id.lexema;
            }

            emparejar("where");
            EXPRCOND(EXPCOND);

            if(buscaTipo(id.entrada) && IGUALACION.tipo.equals(VACIO) && EXPCOND.tipo.equals(VACIO)){
                ACTREGS.tipo = VACIO;
            }
            else{
                ACTREGS.tipo = ERROR_TIPO;
            }

        } else {
            error("[ACTREGS] El programa debe continuar con la sentencia update"
                    + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//Autor: Cabrales Coronado Heber - 13130684

    private void COLUMNAS(Atributos COLUMNAS) {

        Atributos COLUMNAS_P = new Atributos();
        Atributos id = new Atributos;

        if (preAnalisis.equals("id")) {
            id = cmp.be.preAnalisis;
            emparejar("id");
            COLUMNAS_P(COLUMNAS_P);

            if(buscaTipo(id.entrada) && COLUMNAS_P.tipo.equals(VACIO)){
                COLUMNAS.tipo = VACIO;
            }
            else{
                COLUMNAS.tipo = ERROR_TIPO;
            }
        } else {
            error("[COLUMNAS] Para definir una columna es necesario un "
                    + "identificador " + "Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//-------------------------------------------------------------------------
//Fernando Alfonso Caldera Olivas                           15130685
//PRIMEROS(COLUMNAS_P) = {,, empty}
    private void COLUMNAS_P(Atributos COLUMNAS_P) {

        Atributos COLUMNAS = new Atributos();

        if (preAnalisis.equals(",")) {
            //COLUMNAS_P -> , COLUMNAS
            emparejar(",");
            COLUMNAS(COLUMNAS);

            if(COLUMNAS.tipo.equals(VACIO)){
                COLUMNAS_P.tipo = VACIO;
            }
            else{
                COLUMNAS_P.tipo = ERROR_TIPO;
            }


        } else {
            //COLUMNAS_P -> empty
        }
    }

//-------------------------------------------------------------------------
//Fernando Alfonso Caldera Olivas                           15130685
//PRIMEROS(DECLARACION) = {declare, empty}
    private void DECLARACION(Atributos DECLARACION) {

        Atributos TIPO = new Atributos();

        if (preAnalisis.equals("declare")) {
            //DECLARACION -> declare idvar TIPO DECLARACION
            emparejar("declare");
            emparejar("idvar");
            TIPO(TIPO);
            DECLARACION(DECLARACION);
        } else {
            //DECLARACION -> empty
        }
    }

//-------------------------------------------------------------------------
//Fernando Alfonso Caldera Olivas                           15130685
//PRIMEROS(DESPLIEGUE) = {print}
    private void DESPLIEGUE(Atributos DESPLIEGUE) {

        Atributos EXPRARIT = new Atributos();

        if (preAnalisis.equals("print")) {
            //DESPLIEGUE -> print EXPRARIT
            emparejar("print");
            EXPRARIT(EXPRARIT);

            if(EXPRARIT.tipo.equals(VACIO)){
                DESPLIEGUE.tipo = VACIO;
            }
            else{
                DESPLIEGUE.tipo = ERROR_TIPO;
            }

        } else {
            error("[DESPLIEGUE]: Se esperaba \"print\" en linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------------
    //Pedro Gutiérrez Castillo
    //EJERCICIOS DELREG,EXPRESIONES Y EXPRESIONES' 
    //Primeros(DELREG) = {delete}
    //Primeros(EXPRESIONES) = {Primeros(EXPRARIT)}
    //                      = {num,num.num,idvar,literal,id,(}
    //Primeros(EXPRESIONES_P) = {, , empty}
    private void DELREG(Atributos DELREG) {

        Atributos EXPCOND = new Atributos();
        Linea_BE id = new Linea_BE();


        if (preAnalisis.equals("delete")) {
            // DELREG -> delete from id where EXPRCOND
            emparejar("delete");
            emparejar("from");
            id = cmp.be.preAnalisis;
            emparejar("id");
            emparejar("where");
            EXPCOND.ambito = id.lexema;
            EXPRCOND(EXPCOND);
            if(buscaTipo(id.entrada).equals("tabla") && EXPCOND.tipo.equals("boolean")){
                DELREG.tipo = VACIO;
            }
            else{
                DELREG.tipo = ERROR_TIPO;
            }

        } else {
            error("[DELREG]: Se esperaba la sentencia delete-from");
        }
    }

    //--------------------------------------------------------------------------
    private void EXPRESIONES(Atributos EXPRESIONES) {

        Atributos EXPRARIT = new Atributos();
        Atributos EXPRESIONES_P = new Atributos();

        if (preAnalisis.equals("num") || preAnalisis.equals("num.num")
                || preAnalisis.equals("idvar") || preAnalisis.equals("literal")
                || preAnalisis.equals("id") || preAnalisis.equals("(")) {
            //EXPRESIONES -> EXPRARIT   EXPRESIONES’
            //EXPRARIT -> OPERANDO,(EXPRARIT)
            //OPERANDO -> num , num.num , idvar , literal , id
            EXPRARIT(EXPRARIT);
            EXPRESIONES_P(EXPRESIONES_P);

            if(EXPRARIT.tipo.equals(VACIO) && EXPRESIONES_P.tipo.equals(VACIO)){
                EXPRESIONES.tipo = VACIO;
            }
            else{
                EXPRESIONES.tipo = ERROR_TIPO;
            }

        } else {
            error("[EXPRESIONES] : se esperaba la sentencia num");
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void EXPRESIONES_P(Atributos EXPRESIONES_P) {

        Atributos EXPRESIONES = new Atributos();

        if (preAnalisis.equals(",")) {
            //EXPRESIONES -> ,
            emparejar(",");
            EXPRESIONES(EXPRESIONES);

            if(EXPRESIONES.tipo.equals(VACIO)){
                EXPRESIONES_P.tipo = VACIO;
            }
            else{
                EXPRESIONES_P.tipo = ERROR_TIPO;
            }
        } else {
            //EXPRESIONES_P-> empty
        }
    }
    //-----------------------------------
    //OCTAVIO HERNANDEZ AGUILAR No.15130500
//PRIMEROS num,num.num,idvar,literal,id

    private void EXPRARIT(Atributos EXPRARIT) {

        Atributos OPERANDO = new Atributos();
        Atributos EXPRARIT_P = new Atributos();
        Atributos EXPRARIT1 = new Atributos();

        if (preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("idvar") || preAnalisis.equals("literal") || preAnalisis.equals("id")) {
            // EXPARIT-> EXPARIT EXPARIT'
            OPERANDO(OPERANDO);
            EXPRARIT_P(EXPRARIT_P);

        } else if (preAnalisis.equals("(")) {

            if(OPERANDO.tipo.equals(VACIO) && EXPRARIT_P.tipo.equals(VACIO)){
                EXPRARIT.tipo = VACIO;
            }
            else{
                EXPRARIT.tipo = ERROR_TIPO;
            }
        }
        else if (preAnalisis.equals("(")) {
            
            emparejar("(");
            EXPRARIT(EXPRARIT1);
            emparejar(")");
            EXPRARIT_P(EXPRARIT_P);

            if(EXPRARIT1.tipo.equals(VACIO) && EXPRARIT_P.tipo.equals(VACIO)){
                EXPRARIT.tipo = VACIO;
            }
            else{
                EXPRARIT.tipo = ERROR_TIPO;
            }
        } else {
            error("[EXPARIT]: inicio no correcto " + "linea" + cmp.be.preAnalisis.numLinea);
        }
    }

//OCTAVIO HERNANDEZ AGUILAR No.15130500--------------------------------------------------------
//PRIMEROS opsuma, opmult , empty
    private void EXPRARIT_P(Atributos EXPRARIT_P) {

        Atributos EXPRARIT = new Atributos();

        if (preAnalisis.equals("opsuma")) {
            //EXPARIT_P -> opmult
            emparejar("opsuma");
            EXPRARIT(EXPRARIT);
            if(EXPRARIT.tipo.equals(VACIO)){
                EXPRARIT_P.tipo = VACIO;
            }
            else{
                EXPRARIT_P.tipo = ERROR_TIPO;
            }

        } else {
            if (preAnalisis.equals("opmult")) {
                //EXPERIT_P -> opmult
                emparejar("opmult");
                EXPRARIT(EXPRARIT);

                if(EXPRARIT.tipo.equals(VACIO)){
                    EXPRARIT_P.tipo = VACIO;
                }
                else{
                    EXPRARIT_P.tipo = ERROR_TIPO;
                }
            } else {
                // EXPARIT_P -> empty
            }
        }
    }

//OCTAVIO HERNANDEZ AGUILAR No.15130500---------------------------------------------------------
//PRIMEROS (EXPRCOND )= {num | num.num | idvar | literal | id}
    private void EXPRCOND(Atributos EXPRCOND) {

        Atributos EXPRLOG = new Atributos();
        Atributos EXPRREL = new Atributos();

        if (preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("idvar") || preAnalisis.equals("literal") || preAnalisis.equals("id")) {
            // EXPRCOND -> EXPRREL EXPRLOG
            EXPRREL(EXPRREL);
            EXPRLOG(EXPRLOG);

            if(EXPRREL.tipo.equals(VACIO) && EXPRLOG.tipo.equals(VACIO)){
                EXPRCOND.tipo = VACIO;
            }
            else{
                EXPRCOND.tipo = ERROR_TIPO;
            }
        } else {
            error("[EXPRCOND]: inicio no correcto " + "linea" + cmp.be.preAnalisis.numLinea);
        }

    }
//----------------------------------------------------
    //14130579 Luis Alfredo Hernandez Montelongo
// Metodo del procedimiento EXPRREL
//******************************************************** 

    private void EXPRREL(Atributos EXPRREL) {
        
        Atributos EXPRARIT1 = new Atributos();
        Atributos EXPRARIT2 = new Atributos();

        if (preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("idvar") || preAnalisis.equals("literal") || preAnalisis.equals("id")) {
            //EXPRREL -> EXPRARIT oprel EXPRARIT
            EXPRARIT(EXPRARIT1);
            emparejar("oprel");
            EXPRARIT(EXPRARIT2);
            if (analizarSemantica) {
                //Acción semántica
                if (tiposCompatibles(EXPRARIT1.tipo, EXPRARIT2.tipo)
                        && !EXPRARIT1.tipo.equals(ERROR_TIPO) && !EXPRARIT2.tipo.equals(ERROR_TIPO)) {
                    EXPRREL.tipo = VACIO;
                } else {
                    EXPRREL.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[EXPRREL]: Se esperaba la sentencia exprrel " + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//14130579 Luis Alfredo Hernandez Montelongo     
// Metodo del procedimiento EXPRLOG 
//******************************************************** 
    private void EXPRLOG(Atributos EXPRLOG) {
        Atributos EXPRREL = new Atributos();

        if (preAnalisis.equals("and")) {
            //EXPRLOG-->and EXPRREL
            emparejar("and");
            EXPRREL(EXPRREL);
            if (analizarSemantica) {
                //Acción semántica
                EXPRLOG.tipo = EXPRREL.tipo;
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("or")) {
            //EXPRLOG-->or EXPRREL
            emparejar("or");
            EXPRREL(EXPRREL);
            if (analizarSemantica) {
                //Acción semántica
                EXPRLOG.tipo = EXPRREL.tipo;
                //Fin acción semántica
            }
        } else {
            //EXPRLOG--> empty 
            if (analizarSemantica) {
                //Acción semántica
                EXPRLOG.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

//14130579 Luis Alfredo Hernandez Montelongo 
// Metodo del procedimiento ELIMTAB
//******************************************************** 
    private void ELIMTAB(Atributos ELIMTAB) {

        Linea_BE id = new Linea_BE();

        if (preAnalisis.equals("drop")) {
            //ELIMTAB-->drop table id
            emparejar("drop");
            emparejar("table");
            id = cmp.be.preAnalisis;
            emparejar("id");
            if (analizarSemantica) {
                //Acción semántica
                if (buscaTipo(id.entrada).equals("tabla")) {
                    ELIMTAB.tipo = VACIO;
                } else {
                    ELIMTAB.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[ELIMTAB]: Se esperaba la sentencia elimtab " + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------
    // Nombre: JOSE ENRIQUE IBARRA MANRIQUEZ. No. Control: 15130713
//PRIMEROS(IFELSE) = { if }
    private void IFELSE(Atributos IFELSE) {
        Atributos EXPRCOND = new Atributos();
        Atributos SENTENCIAS = new Atributos();
        Atributos IFELSE_P = new Atributos();

        if (preAnalisis.equals("if")) {
            //IFELSE -> if EXPRCOND begin SENTENCIAS end IFELSE_P
            emparejar("if");
            EXPRCOND(EXPRCOND);
            emparejar("begin");
            SENTENCIAS(SENTENCIAS);
            emparejar("end");
            IFELSE_P(IFELSE_P);
            if (analizarSemantica) {
                //Acción semántica
                if (EXPRCOND.tipo.equals("boolean") && SENTENCIAS.tipo.equals(VACIO)
                        && IFELSE_P.tipo.equals(VACIO)) {
                    IFELSE.tipo = VACIO;
                } else {
                    IFELSE.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[IFELSE]: SE ESPERABA UNA SENTENCIA DEL TIPO IF-ELSE " + "No. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//PRIMEROS(IFELSE_P) = { else, empty }
    private void IFELSE_P(Atributos IFELSE_P) {
        Atributos SENTENCIAS = new Atributos();

        if (preAnalisis.equals("else")) {
            //IFELSE_P -> else begin SENTENCIAS end
            emparejar("else");
            emparejar("begin");
            SENTENCIAS(SENTENCIAS);
            emparejar("end");
            if (analizarSemantica) {
                //Acción semántica
                IFELSE_P.tipo = SENTENCIAS.tipo;
                //Fin acción semántica
            }
        } else {
            //IFELSE_P produce empty
            if (analizarSemantica) {
                //Acción semántica
                IFELSE_P.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

//PRIMEROS(IGUALACION) = { id }
    private void IGUALACION(Atributos IGUALACION) {
        Atributos EXPRARIT = new Atributos();
        Atributos IGUALACIONP = new Atributos();
        Linea_BE id = new Linea_BE();

        if (preAnalisis.equals("id")) {
            //IGUALACION -> id opasig EXPRARIT IGUALACIONP
            emparejar("id");
            emparejar("opasig");
            EXPRARIT(EXPRARIT);
            IGUALACIONP(IGUALACIONP);
            if (analizarSemantica) {
                //Acción semántica
                if (tiposCompatibles(buscaTipo(id.entrada), EXPRARIT.tipo)
                        && IGUALACIONP.tipo.equals(VACIO)) {
                    IGUALACION.tipo = VACIO;
                } else {
                    IGUALACION.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[IGUALACION]: SE ESPERABA UNA SENTENCIA DE IGUALACIÓN " + "No. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//---------------------------
//YAIR EMMANUEL MIERELES ORTIZ No.Ctrl: 14130078
    private void IGUALACIONP(Atributos IGUALACIONP) {
        Atributos IGUALACION = new Atributos();

        if (preAnalisis.equals(",")) {//IGUALACIONP -> { , IGUALACION }
            emparejar(",");
            IGUALACION(IGUALACION);
            if (analizarSemantica) {
                //Acción semántica
                IGUALACIONP.tipo = IGUALACION.tipo;
                //Fin acción semántica
            }
        } else {
            //IGUALACIONP -> empty
            if (analizarSemantica) {
                //Acción semántica
                IGUALACIONP.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

    //Yair Emmanuel Mireles Ortiz 14130078
    private void INSERCION(Atributos INSERCION) {
        Atributos COLUMNAS = new Atributos();
        Atributos EXPRESIONES = new Atributos();
        Linea_BE id = new Linea_BE();

        if (preAnalisis.equals("insert")) {
            //INCERCION -> { insert into id ( COLUMNAS ) values ( EXPRESION )}
            emparejar("insert");
            emparejar("into");
            id = cmp.be.preAnalisis;
            emparejar("id");
            emparejar("(");
            COLUMNAS(COLUMNAS);
            emparejar(")");
            emparejar("values");
            emparejar("(");
            EXPRESIONES(EXPRESIONES);
            emparejar(")");
            if (analizarSemantica) {
                //Acción semántica
                if (checarArchivo(id.lexema + ".db") && COLUMNAS.tipo.equals(VACIO)
                        && EXPRESIONES.tipo.equals(VACIO)) {
                    INSERCION.tipo = VACIO;
                } else {
                    INSERCION.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            //nein
            error("[ INCERCION ]: Para realizar INSERCION es necesario la siguiente sentencia insert into id ( COLUMNAS ) values ( EXPRESION ) "
                    + "No.Linea" + cmp.be.preAnalisis.numLinea);
        }
    }

    private void LISTAIDS(Atributos LISTAIDS1) { //Yair Emmanuel Mireles Ortiz 14130078
        Atributos LISTAIDS2 = new Atributos();

        if (preAnalisis.equals(",")) {//LISTAIDS -> { , id LISTAIDS}
            emparejar(",");
            emparejar("id");
            LISTAIDS(LISTAIDS2);
            if (analizarSemantica) {
                //Acción semántica
                LISTAIDS1.tipo = LISTAIDS2.tipo;
                //Fin acción semántica
            }
        } else {
            if (analizarSemantica) {
                //Acción semántica
                LISTAIDS1.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

//------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------
    //MONTES QUIROZ SABINO RUBEN    15130056
    //primeros de NUL(null,not,empty) 
    private void NULO(Atributos NULO) {
        if (preAnalisis.equals("null")) {
            //NULO -> null
            emparejar("null");
            if (analizarSemantica) {
                //Acción semántica
                NULO.tipo = VACIO;
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("not")) {
            //NULO ->not null
            emparejar("not");
            emparejar("null");
            if (analizarSemantica) {
                //Acción semántica
                NULO.tipo = VACIO;
                //Fin acción semántica
            }
        } else {
            //NULO ->empty
            if (analizarSemantica) {
                //Acción semántica
                NULO.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

    //----------------------------------------------------
    //MONTES QUIROZ SABINO RUBEN    15130056
    //primeros de OPERANDO(num,num.num,idvar,literal,id)
    private void OPERANDO(Atributos OPERANDO) {
        Linea_BE idvar = new Linea_BE();
        Linea_BE literal = new Linea_BE();
        Linea_BE id = new Linea_BE();

        if (preAnalisis.equals("num")) {
            //OPERANDO -> num
            emparejar("num");
            if (analizarSemantica) {
                //Acción semántica
                OPERANDO.tipo = "integer";
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("num.num")) {
            //operando -> num.num
            emparejar("num.num");
            if (analizarSemantica) {
                //Acción semántica
                OPERANDO.tipo = "real";
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("idvar")) {
            //operando -> invar
            idvar = cmp.be.preAnalisis;
            emparejar("idvar");
            if (analizarSemantica) {
                //Acción semántica
                OPERANDO.tipo = buscaTipo(idvar.entrada);
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("literal")) {
            //operando -> literal 
            literal = cmp.be.preAnalisis;
            emparejar("literal");
            if (analizarSemantica) {
                //Acción semántica
                OPERANDO.tipo = "char(" + literal.lexema.length() + ")";
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("id")) {
            //operando -> id
            id = cmp.be.preAnalisis;
            emparejar("id");
            if (analizarSemantica) {
                //Acción semántica
                OPERANDO.tipo = buscaTipo(id.entrada);
                //Fin acción semántica
            }
        } else {
            error("[OPERANDO]: Se esperaba \"num | mun.num | invar | literal| id \" en linea " + cmp.be.preAnalisis.numLinea);
        }
        //
    }

    //----------------------------------------------------
    //MONTES QUIROZ SABINO RUBEN    15130056
    //primeros de SENTENCEA(sentencia(), empty)
    private void SENTENCIAS(Atributos SENTENCIAS1) {
        Atributos SENTENCIA = new Atributos();
        Atributos SENTENCIAS2 = new Atributos();

        if (preAnalisis.equals("if")
                || preAnalisis.equals("while")
                || preAnalisis.equals("print")
                || preAnalisis.equals("assign")
                || preAnalisis.equals("select")
                || preAnalisis.equals("delete")
                || preAnalisis.equals("insert")
                || preAnalisis.equals("update")
                || preAnalisis.equals("create")
                || preAnalisis.equals("drop")
                || preAnalisis.equals("case")) {
            //sentencias -> sentencia sentencias 
            SENTENCIA(SENTENCIA);
            SENTENCIAS(SENTENCIAS2);
            if (analizarSemantica) {
                //Acción semántica
                if (SENTENCIA.tipo.equals(VACIO) && SENTENCIAS2.tipo.equals(VACIO)) {
                    SENTENCIAS1.tipo = VACIO;
                } else {
                    SENTENCIAS1.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            //sentencias -> empty
            if (analizarSemantica) {
                //Acción semántica
                SENTENCIAS1.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

    //---------------
    //Autor: Alexis Enrique Noyola Saenz - 14131193
    private void SELECTIVA(Atributos SELECTIVA) {
        
        Atributos SELWHEN = new Atributos();
        Atributos SELELSE = new Atributos();
        
        if (preAnalisis.equals("case")) {
            //SELECTIVA -> case SELWHEN SELELSE end 
            emparejar("case");
            SELWHEN(SELWHEN);
            SELELSE(SELELSE);
            emparejar("end");
            if (analizarSemantica) {
                //Acción semántica
                if(SELWHEN.tipo.equals(VACIO) && SELELSE.tipo.equals(VACIO)) {
                    SELECTIVA.tipo = VACIO;
                } else {
                    SELECTIVA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[SELECTIVA] : < Se esperaba la sentencia 'case' >." + "No Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void SELWHEN(Atributos SELWHEN) {
        
        Atributos EXPRCOND = new Atributos();
        Atributos SENTENCIA = new Atributos();
        Atributos SELWHEN_P = new Atributos();
        
        if (preAnalisis.equals("when")) {
            //SELWHEN -> when EXPRCOND then SENTENCIA SELWHEN'
            emparejar("when");
            EXPRCOND(EXPRCOND);
            emparejar("then");
            SENTENCIA(SENTENCIA);
            SELWHEN_P(SELWHEN_P);
            if (analizarSemantica) {
                //Acción semántica
                if(EXPRCOND.tipo.equals("booleano") && SENTENCIA.tipo.equals(VACIO) && SELWHEN_P.tipo.equals(VACIO)) {
                    SELWHEN.tipo = VACIO;
                } else {
                    SELWHEN.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[SELWHEN] : < Se esperaba la sentencia 'when' >." + "No Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void SENTENCIA(Atributos SENTENCIA) {
        if (preAnalisis.equals("if")) {
            //SENTENCIA -> IFELSE
            Atributos IFELSE = new Atributos();
            IFELSE(IFELSE);
            if (analizarSemantica) {
                //Acción semántica
                if(IFELSE.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("while")) {
            //SENTENCIA -> SENREP
            Atributos SENREP = new Atributos();
            SENREP(SENREP);
            if (analizarSemantica) {
                //Acción semántica
                if(SENREP.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("print")) {
            //SENTENCIA -> DESPLIEGUE
            Atributos DESPLIEGUE = new Atributos();
            DESPLIEGUE(DESPLIEGUE);
            if (analizarSemantica) {
                //Acción semántica
                if(DESPLIEGUE.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("assign")) {
            //SENTENCIA -> SENTASIG
            Atributos SENTASIG = new Atributos();
            SENTASIG(SENTASIG);
            if (analizarSemantica) {
                //Acción semántica
                if(SENTASIG.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("select")) {
            //SENTENCIA -> SENTSELECT
            Atributos SENTSELECT = new Atributos();
            SENTSELECT(SENTSELECT);
            if (analizarSemantica) {
                //Acción semántica
                if(SENTSELECT.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("delete")) {
            //SENTENCIA -> DELREG
            Atributos DELREG = new Atributos();
            DELREG(DELREG);
            if (analizarSemantica) {
                //Acción semántica
                if(DELREG.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("insert")) {
            //SENTENCIA -> INSERCION
            Atributos INSERCION = new Atributos();
            INSERCION(INSERCION);
            if (analizarSemantica) {
                //Acción semántica
                if(INSERCION.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("update")) {
            //SENTENCIA -> ACTREGS
            Atributos ACTREGS = new Atributos();
            ACTREGS(ACTREGS);
            if (analizarSemantica) {
                //Acción semántica
                if(ACTREGS.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("create")) {
            //SENTENCIA -> TABLA
            Atributos TABLA = new Atributos();
            TABLA(TABLA);
            if (analizarSemantica) {
                //Acción semántica
                if(TABLA.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("drop")) {
            //SENTENCIA -> ELIMTAB
            Atributos ELIMTAB = new Atributos();
            ELIMTAB(ELIMTAB);
            if (analizarSemantica) {
                //Acción semántica
                if(ELIMTAB.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("case")) {
            //SENTENCIA -> SELECTIVA
            Atributos SELECTIVA = new Atributos();
            SELECTIVA(SELECTIVA);
            if (analizarSemantica) {
                //Acción semántica
                if(SELECTIVA.tipo.equals(VACIO)) {
                    SENTENCIA.tipo = VACIO;
                } else {
                    SENTENCIA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[SENTENCIA] : < Se esperaba la sentencia SQL valida >." + "No Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//-------------------------------
    //Agustín Pérez Calderón    14130042
    //PRIMEROS(SELWHEN_P) = {PRIMEROS(SELWHEN),empty }
    //----------------------PRIMEROS(SELWHEN) = {when}
    private void SELWHEN_P(Atributos SELWHEN_P) {
        
        Atributos SELWHEN = new Atributos();
        
        if (preAnalisis.equals("when")) {
            //SELWHEN’ -> SELWHEN
            SELWHEN(SELWHEN);
            if (analizarSemantica) {
                //Acción semántica
                if(SELWHEN.tipo.equals(VACIO)) {
                    SELWHEN_P.tipo = VACIO;
                } else {
                    SELWHEN_P.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            //SELWHEN’ -> empty
            if (analizarSemantica) {
                //Acción semántica
                SELWHEN_P.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

    //PRIMEROS(SELELSE) = {else,empty}
    private void SELELSE(Atributos SELELSE) {
        
        Atributos SENTENCIA = new Atributos();
        
        if (preAnalisis.equals("else")) {
            //SELELSE -> else SENTENCIA
            emparejar("else");
            SENTENCIA(SENTENCIA);
            if (analizarSemantica) {
                //Acción semántica
                if(SENTENCIA.tipo.equals(VACIO)) {
                    SELELSE.tipo = VACIO;
                } else {
                    SELELSE.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            //SELELSE -> empty
            if (analizarSemantica) {
                //Acción semántica
                SELELSE.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }

    //PRIMEROS(SENREP) = {while}
    private void SENREP(Atributos SENREP) {
        
        Atributos EXPRCOND = new Atributos();
        Atributos SENTENCIAS = new Atributos();
        
        if (preAnalisis.equals("while")) {
            //SENREP -> while EXPRCOND begin SENTENCIAS end
            emparejar("while");
            EXPRCOND(EXPRCOND);
            emparejar("begin");
            SENTENCIAS(SENTENCIAS);
            emparejar("end");
            if (analizarSemantica) {
                //Acción semántica
                if(EXPRCOND.tipo.equals("booleano") && SENTENCIAS.tipo.equals(VACIO)) {
                    SENREP.tipo = VACIO;
                } else {
                    SENREP.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[SENREP] : Se esperaba la sentencia while" + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }
//-----------------------------------------------------------
//Wendy Guadalupe Ramirez Lucio		#14131244

//Primeros (SENTASIG) = {assign}
//Primeros (SENTSELECT) = {select}
    private void SENTASIG(Atributos SENTASIG) {
        
        Atributos EXPRARIT = new Atributos();
        
        if (preAnalisis.equals("assign")) {
            //SENTASIG -> assign idvar opasig EXPRARIT
            emparejar("assign");
            emparejar("idvar");
            emparejar("opasig");
            EXPRARIT(EXPRARIT);
            if (analizarSemantica) {
                //Acción semántica
                if(EXPRARIT.tipo.equals(VACIO)) {
                    SENTASIG.tipo = VACIO;
                } else {
                    SENTASIG.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[SENTASIG]: Se esperaba la sentencia assign"
                    + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void SENTSELECT(Atributos SENTSELECT) {
        
        Atributos SENTSELECTC = new Atributos();
        Atributos EXPRCOND = new Atributos();
        
        if (preAnalisis.equals("select")) {
            //SENTSELECT -> select idvar opasig id SENTSELECTC from id where EXPRCOND
            emparejar("select");
            emparejar("idvar");
            emparejar("opasig");
            emparejar("id");
            SENTSELECTC(SENTSELECTC);
            emparejar("from");
            emparejar("id");
            emparejar("where");
            EXPRCOND(EXPRCOND);
            if (analizarSemantica) {
                //Acción semántica
                if(SENTSELECTC.tipo.equals(VACIO) && EXPRCOND.tipo.equals("booleano")) {
                    SENTSELECT.tipo = VACIO;
                } else {
                    SENTSELECT.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            error("[SENTSELECT]: Se esperaba la sentencia select" + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//--- Autor: Jose Eduardo Rodriguez Diaz 13130453
    //Primeros (SENTSELECT)= {, , empty}
    private void SENTSELECTC(Atributos SENTSELECTC1) {
        
        Atributos SENTSELECTC2 = new Atributos(); 
        
        if (preAnalisis.equals(",")) {
            //SENTSELECTC -> , idvar opasig id SENTSELECTC
            emparejar(",");
            emparejar("idvar");
            emparejar("opasig");
            emparejar("id");
            SENTSELECTC(SENTSELECTC2);
            if (analizarSemantica) {
                //Acción semántica
                if(SENTSELECTC2.tipo.equals(VACIO)) {
                    SENTSELECTC1.tipo = VACIO;
                } else {
                    SENTSELECTC1.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            //SENTSELECTC -> empty
            if (analizarSemantica) {
                //Acción semántica
                SENTSELECTC1.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }
    //Primeros ( TIPO ) = {int , float , char}

    private void TIPO(Atributos TIPO) {
        if (preAnalisis.equals("int")) {
            // TIPO ---> int
            emparejar("int");
            if (analizarSemantica) {
                //Acción semántica
                TIPO.tipo = "int";
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("float")) {
            // TIPO ---> float
            emparejar("float");
            if (analizarSemantica) {
                //Acción semántica
                TIPO.tipo = "float";
                //Fin acción semántica
            }
        } else if (preAnalisis.equals("char")) {
            //TIPO ---> char (num)
            emparejar("char");
            emparejar("(");
            emparejar("num");
            emparejar(")");
            Linea_BE num = new Linea_BE();
            if (analizarSemantica) {
                //Acción semántica
                TIPO.tipo = "char(" + num.lexema + ")";
                //Fin acción semántica
            }
        } else {
            error("[TIPO] Se esperaba un tipo de dato int, float , char "
                    + "Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void TABLA(Atributos TABLA) {
        //PRIMEROS TABLA = {create}
        Atributos TABCOLUMNAS = new Atributos(); 
        
        if (preAnalisis.equals("create")) {
            //TABLA ---> create table id (TABCOLUMNAS)
            emparejar("create");
            emparejar("table");
            emparejar("id");
            emparejar("(");
            TABCOLUMNAS(TABCOLUMNAS);
            emparejar(")");
            if (analizarSemantica) {
                //Acción semántica
                if(TABCOLUMNAS.tipo.equals(VACIO)) {
                    TABLA.tipo = VACIO;
                } else {
                    TABLA.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }

        } else {
            error("[TABLA] Para crear un tabla es necesario utilizar create"
                    + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    //-------------------------------------------------------
    // David Soto Rodriguez     #14130602
    //Primero(TABCOLUMAS) = { id TIPO NULO TABCOLUMNAS_P }
    private void TABCOLUMNAS(Atributos TABCOLUMNAS) {
        Atributos TIPO = new Atributos();
        Atributos NULO = new Atributos();
        Atributos TABCOLUMNAS_P = new Atributos();
        
        if (preAnalisis.equals("id")) {
            //TABCOLUMNAS -> { id TIPO NULO TABCOLUMNAS_P }
            emparejar("id");
            TIPO(TIPO);
            NULO(NULO);
            TABCOLUMNAS_P(TABCOLUMNAS_P);
            if (analizarSemantica) {
                //Acción semántica
                if(!TIPO.tipo.equals(ERROR_TIPO) && !NULO.tipo.equals(ERROR_TIPO) && TABCOLUMNAS_P.tipo.equals(VACIO)) {
                    TABCOLUMNAS.tipo = VACIO;
                } else {
                    TABCOLUMNAS.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            //error( "En TABCOLUMNAS" );
            //error("[<nombre-procedure> ]: <descripcion del error>"+ " No.Linea" + cmp.be.preAnalisis.numLinea
            //);
            error("[ TABCOLUMAS ]: Para definir TABCOLUMNAS es necesario un identificador "
                    + "No.Linea" + cmp.be.preAnalisis.numLinea);
        }
    }

    //---------------------------------------------------------
    // David Soto Rodriguez     #14130602
    //Primero(TABCOLUMAS_P) = { , TABCOLUMNAS | empty }
    private void TABCOLUMNAS_P(Atributos TABCOLUMNAS_P) {
        Atributos TABCOLUMNAS = new Atributos();
        if (preAnalisis.equals(",")) {
            //TABCOLUMNAS_P -> {, TABCOLUMNAS }
            emparejar(",");
            TABCOLUMNAS(TABCOLUMNAS);
            if (analizarSemantica) {
                //Acción semántica
                if(TABCOLUMNAS.tipo.equals(VACIO)) {
                    TABCOLUMNAS_P.tipo = VACIO;
                } else {
                    TABCOLUMNAS_P.tipo = ERROR_TIPO;
                }
                //Fin acción semántica
            }
        } else {
            //TABCOLUMNAS_P -> empty
            if (analizarSemantica) {
                //Acción semántica
                TABCOLUMNAS_P.tipo = VACIO;
                //Fin acción semántica
            }
        }
    }
}
//------------------------------------------------------------------------------
//::
