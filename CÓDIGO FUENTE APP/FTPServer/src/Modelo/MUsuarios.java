package Modelo;

import java.sql.*;

public class MUsuarios{
	
	private final Connection conexion;
	
	public MUsuarios(Connection conexion){
		this.conexion = conexion;		
	}
	
	
	/**
	 * Función que determina la existencia y la comparación de usuario y contraseña de la tabla usuarios.
	 * @param user Recibe como parámetro el nombre de usuario de la tabla.
	 * @param pass Recibe como parámetro la contraseña de usuario de la tabla.
	 * @return Devolverá true si contador (alias de la consulta) es igual a 1, sino será 0.
	 */
	public boolean accesoLogin(String user, String pass){
		
		// La consulta a realizar consiste en que nos devuelva el número de filas coincidentes de usuario y contraseña.
		String consultaUserPass = "select count(*) as contador from bd_ftp.users where usuario = ? and contrasenia = ?;";
		boolean correcto = false;
		int contador = 0;
		        
        try{
            PreparedStatement sentencia = conexion.prepareStatement(consultaUserPass);
            sentencia.setString(1, user);
            sentencia.setString(2, pass);
            
            ResultSet rs = sentencia.executeQuery();
            
            while(rs.next()){
            	contador = rs.getInt("contador");
            }
            
        }catch(SQLException e){
            System.out.println(e);
        }
        
        	// Si el número de filas devueltas en la consulta es 0, es que no hay coincidencia entre usuario y contraseña.
	        if(contador == 0){
	        	correcto = false;
	        // de lo contrario, es que hubo una coincidencia (1) entre usuario y contraseña.
	        }else{
	        	correcto = true;
	        }
	               
        return correcto;
	}

}
