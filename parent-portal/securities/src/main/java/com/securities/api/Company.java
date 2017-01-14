package com.securities.api;

import java.io.IOException;
import java.util.UUID;

import com.infrastructure.core.Recordable;

public interface Company extends Recordable<UUID> {
	
	String denomination() throws IOException;
	String rccm() throws IOException;
	String ncc() throws IOException;
	String siegeSocial() throws IOException;
	String bp() throws IOException;
	String tel() throws IOException;
	String fax() throws IOException;
	String email() throws IOException;
	String webSite() throws IOException;
	String logo() throws IOException;
	
	Company update( String denomination, 
		            String rccm, 
		            String ncc, 
		            String siegeSocial, 
		            String bp, 
		            String tel,
		            String fax,
		            String email, 
		            String webSite, 
		            String logo) throws IOException;
	
	void changeLogo(String logo) throws IOException;	
	Modules modules();	
	Membership membership();	
	Persons persons();
	Sequences sequences();
	MesureUnits mesureUnits();
	MesureUnitTypes mesureUnitTypes();
}
