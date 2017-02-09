package com.securities.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.ws.rs.NotFoundException;

import org.apache.commons.lang3.StringUtils;

import com.common.utilities.convert.UUIDConvert;
import com.infrastructure.core.impl.HorodateImpl;
import com.infrastructure.datasource.Base;
import com.infrastructure.datasource.DomainStore;
import com.infrastructure.datasource.DomainsStore;
import com.infrastructure.datasource.Base.OrderDirection;
import com.securities.api.Company;
import com.securities.api.Tax;
import com.securities.api.TaxMetadata;
import com.securities.api.Taxes;

public class TaxesImpl implements Taxes {

	private final transient Base base;
	private final transient TaxMetadata dm;
	private final transient DomainsStore ds;
	private final transient Company company;
	
	public TaxesImpl(final Base base, final Company company){
		this.base = base;
		this.dm = TaxImpl.dm();
		this.ds = base.domainsStore(dm);
		this.company = company;
	}
	
	@Override
	public Tax get(UUID id) throws IOException {
		Tax item = build(id);
		
		if(!item.isPresent() || !item.company().isEqual(company))
			throw new NotFoundException("La taxe n'a pas �t� trouv� !");
		
		return build(id);
	}

	@Override
	public List<Tax> all() throws IOException {
		List<Tax> items = new ArrayList<Tax>();
		
		List<DomainStore> dss = ds.getAllOrdered(HorodateImpl.dm().dateCreatedKey(), OrderDirection.DESC);
		
		for (DomainStore domainStore : dss) {
			items.add(build(UUIDConvert.fromObject(domainStore.key())));
		}
		
		return items;
	}
	
	private Tax build(UUID id){
		return new TaxImpl(base, id);
	}

	@Override
	public Tax add(String name, String shortName, int rate) throws IOException {
		
		if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Invalid name : it can't be empty!");
        }
		
		if (StringUtils.isBlank(shortName)) {
            throw new IllegalArgumentException("Invalid short name : it can't be zero!");
        }
		
		if (rate <= 0) {
            throw new IllegalArgumentException("Invalid rate : it can't be negative or zero!");
        }
				
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(dm.nameKey(), name);
		params.put(dm.shortNameKey(), shortName);
		params.put(dm.rateKey(), rate);
		params.put(dm.companyIdKey(), company.id());
		
		UUID id = UUID.randomUUID();
		ds.set(id, params);
		
		return build(id);
	}

	@Override
	public void delete(Tax item) throws IOException {
		if(item.isPresent() && item.company().isEqual(company))
		ds.delete(item.id());
	}
}
