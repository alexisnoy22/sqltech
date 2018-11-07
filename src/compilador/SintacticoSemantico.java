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
                || preAnalisis.equals("assign")      || preAnalisis.equals("case")
                || preAnalisis.equals("end")) {
            DECLARACION();
            SENTENCIAS();
            emparejar("end");

            if(DECLARACION.tipo.equals('VACIO') && SENTENCIAS.tipo.equals('VACIO')){
                PROGRAMASQL.tipo = 'VACIO';
            }
            else{
                PROGRAMASQL.tipo = 'ERROR_TIPO';
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
        Atributos id = new Atributos();

        if (preAnalisis.equals("update")) {
            emparejar("update");
            id = cmp.be.preAnalisis;
            emparejar("id");
            emparejar("set");
            IGUALACION();
            emparejar("where");
            EXPRCOND();

            if(buscaTipo(id.entrada) && IGUALACION.tipo == 'VACIO' && EXPCOND.tipo == 'VACIO'){
                ACTREGS.tipo = 'VACIO';
            }
            else{
                ACTREGS.tipo = 'ERROR_TIPO';
            }

        } else {
            error("[ACTREGS] El programa debe continuar con la sentencia update"
                    + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//Autor: Cabrales Coronado Heber - 13130684

    private void COLUMNAS(Atributos COLUMNAS) {

        Atributos COLUMNAS1 = new Atributos();
        Atributos id = new Atributos;

        if (preAnalisis.equals("id")) {
            id = cmp.be.preAnalisis;
            emparejar("id");
            COLUMNAS_P();

            if(buscaTipo(id.entrada) && COLUMNAS_P.tipo == 'VACIO'){
                COLUMNAS.tipo = 'VACIO';
            }
            else{
                COLUMNAS.tipo = 'ERROR_TIPO';
            }
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

        }
        else if (preAnalisis.equals("(")) {
            
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
private void EXPRREL() {
        if(preAnalisis.equals("num") || preAnalisis.equals("num.num") || preAnalisis.equals("idvar") || preAnalisis.equals("literal") || preAnalisis.equals("id")){
            //EXPRREL -> EXPRARIT oprel EXPRARIT
            EXPRARIT();
            emparejar("oprel");
            EXPRARIT();
        }
        else{
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
    private void SELECTIVA() {
        if (preAnalisis.equals("case")) {
            //SELECTIVA -> case SELWHEN SELELSE end 
            emparejar("case");
            SELWHEN();
            SELELSE();
            emparejar("end");
        } else {
            error("[SELECTIVA] : < Se esperaba la sentencia 'case' >." + "No Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void SELWHEN() {
        if (preAnalisis.equals("when")) {
            //SELWHEN -> when EXPRCOND then SENTENCIA SELWHEN'
            emparejar("when");
            EXPRCOND();
            emparejar("then");
            SENTENCIA();
            SELWHEN_P();
        } else {
            error("[SELWHEN] : < Se esperaba la sentencia 'when' >." + "No Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void SENTENCIA() {
        if (preAnalisis.equals("if")) {
            //SENTENCIA -> IFELSE
            IFELSE();
        } else if (preAnalisis.equals("while")) {
            //SENTENCIA -> SENREP
            SENREP();
        } else if (preAnalisis.equals("print")) {
            //SENTENCIA -> DESPLIEGUE
            DESPLIEGUE();
        } else if (preAnalisis.equals("assign")) {
            //SENTENCIA -> SENTASIG
            SENTASIG();
        } else if (preAnalisis.equals("select")) {
            //SENTENCIA -> SENTSELECT
            SENTSELECT();
        } else if (preAnalisis.equals("delete")) {
            //SENTENCIA -> DELREG
            DELREG();
        } else if (preAnalisis.equals("insert")) {
            //SENTENCIA -> INSERCION
            INSERCION();
        } else if (preAnalisis.equals("update")) {
            //SENTENCIA -> ACTREGS
            ACTREGS();
        } else if (preAnalisis.equals("create")) {
            //SENTENCIA -> TABLA
            TABLA();
        } else if (preAnalisis.equals("drop")) {
            //SENTENCIA -> ELIMTAB
            ELIMTAB();
        } else if (preAnalisis.equals("case")) {
            //SENTENCIA -> SELECTIVA
            SELECTIVA();
        } else {
            error("[SENTENCIA] : < Se esperaba la sentencia SQL valida >." + "No Linea: " + cmp.be.preAnalisis.numLinea);
        }
    }

//-------------------------------
    //Agustín Pérez Calderón    14130042
    //PRIMEROS(SELWHEN_P) = {PRIMEROS(SELWHEN),empty }
    //----------------------PRIMEROS(SELWHEN) = {when}
    private void SELWHEN_P() {
        if (preAnalisis.equals("when")) {
            //SELWHEN’ -> SELWHEN
            SELWHEN();
        } else {
            //SELWHEN’ -> empty
        }
    }

    //PRIMEROS(SELELSE) = {else,empty}
    private void SELELSE() {
        if (preAnalisis.equals("else")) {
            //SELELSE -> else SENTENCIA
            emparejar("else");
            SENTENCIA();
        } else {
            //SELELSE -> empty
        }
    }

    //PRIMEROS(SENREP) = {while}
    private void SENREP() {
        if (preAnalisis.equals("while")) {
            //SENREP -> while EXPRCOND begin SENTENCIAS end
            emparejar("while");
            EXPRCOND();
            emparejar("begin");
            SENTENCIAS();
            emparejar("end");
        } else {
            error("[SENREP] : Se esperaba la sentencia while" + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }
//-----------------------------------------------------------
//Wendy Guadalupe Ramirez Lucio		#14131244

//Primeros (SENTASIG) = {assign}
//Primeros (SENTSELECT) = {select}
    private void SENTASIG() {
        if (preAnalisis.equals("assign")) {
            //SENTASIG -> assign idvar opasig EXPRARIT
            emparejar("assign");
            emparejar("idvar");
            emparejar("opasig");
            EXPRARIT();
        } else {
            error("[SENTASIG]: Se esperaba la sentencia assign"
                    + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void SENTSELECT() {
        if (preAnalisis.equals("select")) {
            //SENTSELECT -> select idvar opasig id SENTSELECTC from id where EXPRCOND
            emparejar("select");
            emparejar("idvar");
            emparejar("opasig");
            emparejar("id");
            SENTSELECTC();
            emparejar("from");
            emparejar("id");
            emparejar("where");
            EXPRCOND();
        } else {
            error("[SENTSELECT]: Se esperaba la sentencia select" + " No. Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

//--- Autor: Jose Eduardo Rodriguez Diaz 13130453
    //Primeros (SENTSELECT)= {, , empty}
    private void SENTSELECTC() {
        if (preAnalisis.equals(",")) {
            //SENTSELECTC -> , idvar opasig id SENTSELECTC
            emparejar(",");
            emparejar("idvar");
            emparejar("opasig");
            emparejar("id");
            SENTSELECTC();

        } else {
            //SENTSELECTC -> empty
        }
    }
    //Primeros ( TIPO ) = {int , float , char}

    private void TIPO() {
        if (preAnalisis.equals("int")) {
            // TIPO ---> int
            emparejar("int");
        } else if (preAnalisis.equals("float")) {
            // TIPO ---> float
            emparejar("float");
        } else if (preAnalisis.equals("char")) {
            //TIPO ---> char (num)
            emparejar("char");
            emparejar("(");
            emparejar("num");
            emparejar(")");
        } else {
            error("[TIPO] Se esperaba un tipo de dato int, float , char "
                    + "Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    private void TABLA() {
        //PRIMEROS TABLA = {create}
        if (preAnalisis.equals("create")) {
            //TABLA ---> create table id (TABCOLUMNAS)
            emparejar("create");
            emparejar("table");
            emparejar("id");
            emparejar("(");
            TABCOLUMNAS();
            emparejar(")");

        } else {
            error("[TABLA] Para crear un tabla es necesario utilizar create"
                    + " Linea " + cmp.be.preAnalisis.numLinea);
        }
    }

    //-------------------------------------------------------
    // David Soto Rodriguez     #14130602
    //Primero(TABCOLUMAS) = { id TIPO NULO TABCOLUMNAS_P }
    private void TABCOLUMNAS() {
        if (preAnalisis.equals("id")) {
            //TABCOLUMNAS -> { id TIPO NULO TABCOLUMNAS_P }
            emparejar("id");
            TIPO();
            NULO();
            TABCOLUMNAS_P();
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
    private void TABCOLUMNAS_P() {
        if (preAnalisis.equals(",")) {
            //TABCOLUMNAS_P -> {, TABCOLUMNAS }
            emparejar(",");
            TABCOLUMNAS();
        } else {
            //TABCOLUMNAS_P -> empty
        }
    }
}
//------------------------------------------------------------------------------
//::
