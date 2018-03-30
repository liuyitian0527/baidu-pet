package com.fun.zpetchain.model;

import java.util.List;

/**
 * bean of pet market, a list of pets
 * <br><b>Copyright 2018 the original author or authors.</b>
 * @author 2bears
 * @since
 * @version 1.0
 */
public class PetsOnSale {
	
	private List<Pet> petsOnSale;

	/**
	 * @return the petsOnSale
	 */
	public List<Pet> getPetsOnSale() {
		return petsOnSale;
	}

	/**
	 * @param petsOnSale the petsOnSale to set
	 */
	public void setPetsOnSale(List<Pet> petsOnSale) {
		this.petsOnSale = petsOnSale;
	}
	
	
}
