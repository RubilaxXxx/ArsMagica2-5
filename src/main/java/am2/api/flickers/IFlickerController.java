package am2.api.flickers;

public interface IFlickerController{
	public byte[] getMetadata(IFlickerFunctionality operator);

	public void setMetadata(IFlickerFunctionality operator, byte[] meta);

	public void removeMetadata(IFlickerFunctionality operator);
}
