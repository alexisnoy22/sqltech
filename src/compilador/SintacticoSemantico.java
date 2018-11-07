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
 *:-----------------------------------------------------------------------------
 */
package compilador;

import javax.swing.JOptionPane;

public class SintacticoSemantico {

    private Compilador cmp;
    private boolean analizarSemantica = false;
    private String preAnalisis;
    public static final String VACIO = "vacio";
    public static final String ERROR_TIPO = "error_tipo";

    public static boolean tiposCompatibles(String tipo1, String tipo2) {
        return false;
    }

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
    private void PROGRAMASQL() {
        if (preAnalisis.equals("declare") || preAnalisis.equals("if")
                || preAnalisis.equals("while") || preAnalisis.equals("print")
                || preAnalisis.equals("select") || preAnalisis.equals("delete")
                || preAnalisis.equals("insert") || preAnalisis.equals("update")
                || preAnalisis.equals("create") || preAnalisis.equals("drop")
                || preAnalisis.equals("assign") || preAnalisis.equals("case")
                || preAnalisis.equals("end")) {
            DECLARACION();
            SENTENCIAS();
            emparejar("end");
        } else {
            error("[PROGRAMASQL] inicio no correcto" + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//Autor: Cabrales Coronado Heber - 13130684
    private void ACTREGS() {
        if (preAnalisis.equals("update")) {
            emparejar("update");
            emparejar("id");
            emparejar("set");
            IGUALACION();
            emparejar("where");
            EXPRCOND();
        } else {
            error("[ACTREGS] El programa debe continuar con la sentencia update"
                    + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//Autor: Cabrales Coronado Heber - 13130684

    private void COLUMNAS() {
        if (preAnalisis.equals("id")) {
            emparejar("id");
            COLUMNAS_P();
        } else {
            error("[COLUMNAS] Para definir una columna es necesario un "
                    + "identificador " + "Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//-------------------------------------------------------------------------
//Fernando Alfonso Caldera Olivas                           15130685
//PRIMEROS(COLUMNAS_P) = {,, empty}
    private void COLUMNAS_P() {
        if (preAnalisis.equals(",")) {
            //COLUMNAS_P -> , COLUMNAS
            emparejar(",");
            COLUMNAS();
        } else {
            //COLUMNAS_P -> empty
        }
    }

//-------------------------------------------------------------------------
//Fernando Alfonso Caldera Olivas                           15130685
//PRIMEROS(DECLARACION) = {declare, empty}
    private void DECLARACION() {
        if (preAnalisis.equals("declare")) {
            //DECLARACION -> declare idvar TIPO DECLARACION
            emparejar("declare");
            emparejar("idvar");
            TIPO();
            DECLARACION();
        } else {
            //DECLARACION -> empty
        }
    }

//-------------------------------------------------------------------------
//Fernando Alfonso Caldera Olivas                           15130685
//PRIMEROS(DESPLIEGUE) = {print}
    private void DESPLIEGUE() {
        if (preAnalisis.equals("print")) {
            //DESPLIEGUE -> print EXPRARIT
            emparejar("print");
            EXPRARIT();
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
    private void DELREG() {
        if (preAnalisis.equals("delete")) {
            // DELREG -> delete from id where EXPRCOND
            emparejar("delete");
            emparejar("from");
            emparejar("id");
            emparejar("where");
            EXPRCOND();
        } else {
            error("[DELREG]: Se esperaba la sentencia delete-from");
        }
    }

    //--------------------------------------------------------------------------
    private void EXPRESIONES() {
        if (preAnalisis.equals("num") || preAnalisis.equals("num.num")
                || preAnalisis.equals("idvar") || preAnalisis.equals("literal")
                || preAnalisis.equals("id") || preAnalisis.equals("(")) {
            //EXPRESIONES -> EXPRARIT   EXPRESIONES’
            //EXPRARIT -> OPERANDO,(EXPRARIT)
            //OPERANDO -> num , num.num , idvar , literal , id
            EXPRARIT();
            EXPRESIONES_P();
        } else {
            error("[EXPRESIONES] : se esperaba la sentencia num");
        }
    }
    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private void EXPRESIONES_P() {
        if (preAnalisis.equals(",")) {
            //EXPRESIONES -> ,
            emparejar(",");
            EXPRESIONES();
        } else {
            //EXPRESIONES_P-> empty
        }
    }
    //-----------------------------------
    //OCTAVIO HERNANDEZ AGUILAR No.15130500
//PRIMEROS num,num.num,idvar,literal,id

    private void EXPRARIT() {

        if (preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("idvar") || preAnalisis.equals("literal") || preAnalisis.equals("id")) {
            // EXPARIT-> EXPARIT EXPARIT'
            OPERANDO();
            EXPRARIT_P();

        } else if (preAnalisis.equals("(")) {

            emparejar("(");
            EXPRARIT();
            emparejar(")");
            EXPRARIT_P();
        } else {
            error("[EXPARIT]: inicio no correcto " + "linea" + cmp.be.preAnalisis.numLinea);
        }
    }

//OCTAVIO HERNANDEZ AGUILAR No.15130500--------------------------------------------------------
//PRIMEROS opsuma, opmult , empty
    private void EXPRARIT_P() {
        if (preAnalisis.equals("opsuma")) {
            //EXPARIT_P -> opmult
            emparejar("opsuma");
            EXPRARIT();
        } else {
            if (preAnalisis.equals("opmult")) {
                //EXPERIT_P -> opmult
                emparejar("opmult");
                EXPRARIT();
            } else {
                // EXPARIT_P -> empty
            }
        }
    }

//OCTAVIO HERNANDEZ AGUILAR No.15130500---------------------------------------------------------
//PRIMEROS (EXPRCOND )= {num | num.num | idvar | literal | id}
    private void EXPRCOND() {
        if (preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("idvar") || preAnalisis.equals("literal") || preAnalisis.equals("id")) {
            // EXPRCOND -> EXPRCOND EXPRREL
            EXPRREL();
            EXPRLOG();
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
            if (tiposCompatibles(EXPRARIT1.tipo, EXPRARIT2.tipo)
                    && !EXPRARIT1.tipo.equals(ERROR_TIPO) && !EXPRARIT2.tipo.equals(ERROR_TIPO)) {
                EXPRREL.tipo = VACIO;
            } else {
                EXPRREL.tipo = ERROR_TIPO;
            }
        } else {
            error("[EXPRREL]: Se esperaba la sentencia exprrel " + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//14130579 Luis Alfredo Hernandez Montelongo     
// Metodo del procedimiento EXPRLOG 
//******************************************************** 
    private void EXPRLOG() {
        if (preAnalisis.equals("and")) {
            //EXPRLOG-->and EXPRREL
            emparejar("and");
            EXPRREL();
        } else if (preAnalisis.equals("or")) {
            //EXPRLOG-->or EXPRREL
            emparejar("or");
            EXPRREL();
        } else {
            //EXPRLOG--> empty 
        }
    }

//14130579 Luis Alfredo Hernandez Montelongo 
// Metodo del procedimiento ELIMTAB
//******************************************************** 
    private void ELIMTAB() {
        if (preAnalisis.equals("drop")) {
            //ELIMTAB-->drop table id
            emparejar("drop");
            emparejar("table");
            emparejar("id");
        } else {
            error("[ELIMTAB]: Se esperaba la sentencia elimtab " + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    //--------------------------------------------------------------------
    // Nombre: JOSE ENRIQUE IBARRA MANRIQUEZ. No. Control: 15130713
//PRIMEROS(IFELSE) = { if }
    private void IFELSE() {
        if (preAnalisis.equals("if")) {
            //IFELSE -> if EXPRCOND begin SENTENCIAS end IFELSE_P
            emparejar("if");
            EXPRCOND();
            emparejar("begin");
            SENTENCIAS();
            emparejar("end");
            IFELSE_P();
        } else {
            error("[IFELSE]: SE ESPERABA UNA SENTENCIA DEL TIPO IF-ELSE " + "No. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//PRIMEROS(IFELSE_P) = { else, empty }
    private void IFELSE_P() {
        if (preAnalisis.equals("else")) {
            //IFELSE_P -> else begin SENTENCIAS end
            emparejar("else");
            emparejar("begin");
            SENTENCIAS();
            emparejar("end");
        } else {
            //IFELSE_P produce empty
        }
    }

//PRIMEROS(IGUALACION) = { id }
    private void IGUALACION() {
        if (preAnalisis.equals("id")) {
            //IGUALACION -> id opasig EXPRARIT IGUALACIONP
            emparejar("id");
            emparejar("opasig");
            EXPRARIT();
            IGUALACIONP();
        } else {
            error("[IGUALACION]: SE ESPERABA UNA SENTENCIA DE IGUALACIÓN " + "No. Línea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//---------------------------
//YAIR EMMANUEL MIERELES ORTIZ No.Ctrl: 14130078
    private void IGUALACIONP() {
        if (preAnalisis.equals(",")) {//IGUALACIONP -> { , IGUALACION }
            emparejar(",");
            IGUALACION();
        } else {
            //IGUALACIONP -> empty
        }
    }

    //Yair Emmanuel Mireles Ortiz 14130078
    private void INSERCION() {
        if (preAnalisis.equals("insert")) {
            //INCERCION -> { insert into id ( COLUMNAS ) values ( EXPRESION )}
            emparejar("insert");
            emparejar("into");
            emparejar("id");
            emparejar("(");
            COLUMNAS();
            emparejar(")");
            emparejar("values");
            emparejar("(");
            EXPRESIONES();
            emparejar(")");
        } else {
            //nein
            error("[ INCERCION ]: Para realizar INSERCION es necesario la siguiente sentencia insert into id ( COLUMNAS ) values ( EXPRESION ) "
                    + "No.Linea" + cmp.be.preAnalisis.numLinea);
        }
    }

    private void LISTAIDS() { //Yair Emmanuel Mireles Ortiz 14130078
        if (preAnalisis.equals(",")) {//LISTAIDS -> { , id LISTAIDS}
            emparejar(",");
            emparejar("id");
            LISTAIDS();
        } else {
            error("[ LISTAIDS ]: Para realizar INSERCION es necesario iniciar con una coma (,) "
                    + "No.Linea" + cmp.be.preAnalisis.numLinea);
        }
    }

//------------------------------------------------------------------------------------------------------------------------
    //----------------------------------------------------
    //MONTES QUIROZ SABINO RUBEN    15130056
    //primeros de NUL(null,not,empty) 
    private void NULO() {
        if (preAnalisis.equals("null")) {
            //NULO -> null
            emparejar("null");
        } else if (preAnalisis.equals("not")) {
            //NULO ->not null
            emparejar("not");
            emparejar("null");
        } else {
            //NULO ->empty
        }
    }

    //----------------------------------------------------
    //MONTES QUIROZ SABINO RUBEN    15130056
    //primeros de OPERANDO(num,num.num,idvar,literal,id)
    private void OPERANDO() {
        if (preAnalisis.equals("num")) {
            //OPERANDO -> num
            emparejar("num");
        } else if (preAnalisis.equals("num.num")) {
            //operando -> num.num
            emparejar("num.num");
        } else if (preAnalisis.equals("idvar")) {
            //operando -> invar
            emparejar("idvar");
        } else if (preAnalisis.equals("literal")) {
            //operando -> literal 
            emparejar("literal");
        } else if (preAnalisis.equals("id")) {
            //operando -> id
            emparejar("id");
        } else {
            error("[OPERANDO]: Se esperaba \"num | mun.num | invar | literal| id \" en linea " + cmp.be.preAnalisis.numLinea);
        }
        //
    }

    //----------------------------------------------------
    //MONTES QUIROZ SABINO RUBEN    15130056
    //primeros de SENTENCEA(sentencia(), empty)
    private void SENTENCIAS() {
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
            SENTENCIA();
            SENTENCIAS();
        } else {
            //sentencias -> empty

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
            if(SELWHEN.tipo.equals(VACIO) && SELELSE.tipo.equals(VACIO)) {
                SELECTIVA.tipo = VACIO;
            } else {
                SELECTIVA.tipo = ERROR_TIPO;
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
            if(EXPRCOND.tipo.equals("booleano") && SENTENCIA.tipo.equals(VACIO) && SELWHEN_P.tipo.equals(VACIO)) {
                SELWHEN.tipo = VACIO;
            } else {
                SELWHEN.tipo = ERROR_TIPO;
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
            if(IFELSE.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("while")) {
            //SENTENCIA -> SENREP
            Atributos SENREP = new Atributos();
            SENREP(SENREP);
            if(SENREP.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("print")) {
            //SENTENCIA -> DESPLIEGUE
            Atributos DESPLIEGUE = new Atributos();
            DESPLIEGUE(DESPLIEGUE);
            if(DESPLIEGUE.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("assign")) {
            //SENTENCIA -> SENTASIG
            Atributos SENTASIG = new Atributos();
            SENTASIG(SENTASIG);
            if(SENTASIG.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("select")) {
            //SENTENCIA -> SENTSELECT
            Atributos SENTSELECT = new Atributos();
            SENTSELECT(SENTSELECT);
            if(SENTSELECT.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("delete")) {
            //SENTENCIA -> DELREG
            Atributos DELREG = new Atributos();
            DELREG(DELREG);
            if(DELREG.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("insert")) {
            //SENTENCIA -> INSERCION
            Atributos INSERCION = new Atributos();
            INSERCION(INSERCION);
            if(INSERCION.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("update")) {
            //SENTENCIA -> ACTREGS
            Atributos ACTREGS = new Atributos();
            ACTREGS(ACTREGS);
            if(ACTREGS.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("create")) {
            //SENTENCIA -> TABLA
            Atributos TABLA = new Atributos();
            TABLA(TABLA);
            if(TABLA.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("drop")) {
            //SENTENCIA -> ELIMTAB
            Atributos ELIMTAB = new Atributos();
            ELIMTAB(ELIMTAB);
            if(ELIMTAB.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
            }
        } else if (preAnalisis.equals("case")) {
            //SENTENCIA -> SELECTIVA
            Atributos SELECTIVA = new Atributos();
            SELECTIVA(SELECTIVA);
            if(SELECTIVA.tipo.equals(VACIO)) {
                SENTENCIA.tipo = VACIO;
            } else {
                SENTENCIA.tipo = ERROR_TIPO;
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
            if(SELWHEN.tipo.equals(VACIO)) {
                SELWHEN_P.tipo = VACIO;
            } else {
                SELWHEN_P.tipo = ERROR_TIPO;
            }
        } else {
            //SELWHEN’ -> empty
            SELWHEN_P.tipo = VACIO;
        }
    }

    //PRIMEROS(SELELSE) = {else,empty}
    private void SELELSE(Atributos SELELSE) {
        
        Atributos SENTENCIA = new Atributos();
        
        if (preAnalisis.equals("else")) {
            //SELELSE -> else SENTENCIA
            emparejar("else");
            SENTENCIA(SENTENCIA);
            if(SENTENCIA.tipo.equals(VACIO)) {
                SELELSE.tipo = VACIO;
            } else {
                SELELSE.tipo = ERROR_TIPO;
            }
        } else {
            //SELELSE -> empty
            SELELSE.tipo = VACIO;
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
            if(EXPRCOND.tipo.equals("booleano") && SENTENCIAS.tipo.equals(VACIO)) {
                SENREP.tipo = VACIO;
            } else {
                SENREP.tipo = ERROR_TIPO;
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
            if(EXPRARIT.tipo.equals(VACIO)) {
                SENTASIG.tipo = VACIO;
            } else {
                SENTASIG.tipo = ERROR_TIPO;
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
            
            if(SENTSELECTC.tipo.equals(VACIO) && EXPRCOND.tipo.equals("booleano")) {
                SENTSELECT.tipo = VACIO;
            } else {
                SENTSELECT.tipo = ERROR_TIPO;
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
            if(SENTSELECTC2.tipo.equals(VACIO)) {
                SENTSELECTC1.tipo = VACIO;
            } else {
                SENTSELECTC1.tipo = ERROR_TIPO;
            }
        } else {
            //SENTSELECTC -> empty
        }
    }
    //Primeros ( TIPO ) = {int , float , char}

    private void TIPO(Atributos TIPO) {
        if (preAnalisis.equals("int")) {
            // TIPO ---> int
            emparejar("int");
            TIPO.tipo = "int";
        } else if (preAnalisis.equals("float")) {
            // TIPO ---> float
            emparejar("float");
            TIPO.tipo = "float";
        } else if (preAnalisis.equals("char")) {
            //TIPO ---> char (num)
            emparejar("char");
            emparejar("(");
            emparejar("num");
            emparejar(")");
            Linea_BE num = new Linea_BE();
            TIPO.tipo = "char(" + num.lexema + ")";
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
            
            if(TABCOLUMNAS.tipo.equals(VACIO)) {
                TABLA.tipo = VACIO;
            } else {
                TABLA.tipo = ERROR_TIPO;
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
            if(!TIPO.tipo.equals(ERROR_TIPO) && !NULO.tipo.equals(ERROR_TIPO) && TABCOLUMNAS_P.tipo.equals(VACIO)) {
                TABCOLUMNAS.tipo = VACIO;
            } else {
                TABCOLUMNAS.tipo = ERROR_TIPO;
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
            if(TABCOLUMNAS.tipo.equals(VACIO)) {
                TABCOLUMNAS_P.tipo = VACIO;
            } else {
                TABCOLUMNAS_P.tipo = ERROR_TIPO;
            }
        } else {
            //TABCOLUMNAS_P -> empty
            TABCOLUMNAS_P.tipo = VACIO;
        }
    }
}
//------------------------------------------------------------------------------
//::
