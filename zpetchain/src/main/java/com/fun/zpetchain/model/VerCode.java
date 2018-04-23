package com.fun.zpetchain.model;

public class VerCode {
	private String seed;
	private String vCode;
	private Long createTime;

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public String getvCode() {
		return vCode;
	}

	public void setvCode(String vCode) {
		this.vCode = vCode;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	@Override
	public String toString() {
		return "VerCode [seed=" + seed + ", vCode=" + vCode + ", createTime=" + createTime + "]";
	}

	
	
}
