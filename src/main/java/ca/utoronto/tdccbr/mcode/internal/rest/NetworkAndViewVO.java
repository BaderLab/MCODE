package ca.utoronto.tdccbr.mcode.internal.rest;

public class NetworkAndViewVO {

	private Long networkId;
	private Long networkViewId;

	public NetworkAndViewVO(Long networkId, Long networkViewId) {
		this.networkId = networkId;
		this.networkViewId = networkViewId;
	}

	public Long getNetworkId() {
		return networkId;
	}

	public void setNetworkId(Long networkId) {
		this.networkId = networkId;
	}

	public Long getNetworkViewId() {
		return networkViewId;
	}

	public void setNetworkViewId(Long networkViewId) {
		this.networkViewId = networkViewId;
	}
}
