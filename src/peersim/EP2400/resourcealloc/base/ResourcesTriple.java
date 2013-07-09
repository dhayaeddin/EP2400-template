package peersim.EP2400.resourcealloc.base;

public class ResourcesTriple {

	
	
	private final double cpuResource;
	private final double memoryResource;
	private final double networkResource;
	
	public ResourcesTriple(double cpuResource, double memoryResource, double networkResource)
	{
		
		this.cpuResource     = cpuResource;
		this.memoryResource  = memoryResource;
		this.networkResource = networkResource;
		
	}
	
	public double getCpuResource() {
		return cpuResource;
	}
	
	public double getMemoryResource() {
		return memoryResource;
	}
	public double getNetworkResource() {
		return networkResource;
	}
	
	
	public double twoNorm()
	{
		  return Math.sqrt(Math.pow(cpuResource, 2) + Math.pow( memoryResource,2) + Math.pow(networkResource,2));		
	}
	
	public ResourcesTriple abs()
	{
		return new ResourcesTriple( Math.abs(cpuResource) , Math.abs(memoryResource), Math.abs(networkResource));
	}
	
	public ResourcesTriple plus(ResourcesTriple a)
	{
		return new ResourcesTriple(cpuResource    + a.cpuResource, 
								   memoryResource + a.memoryResource, 
								  networkResource + a.networkResource );
	}
	
	public ResourcesTriple minus(ResourcesTriple b)
	{
		return new ResourcesTriple(cpuResource    - b.cpuResource, 
								   memoryResource - b.memoryResource, 
								  networkResource - b.networkResource );
	}
	
	public ResourcesTriple divideConstant(double c)
	{
		return new ResourcesTriple(cpuResource    / c, 
								   memoryResource / c, 
								  networkResource / c);
	}
	
	public ResourcesTriple pairWiseDivision(ResourcesTriple deno)
	{
		return new ResourcesTriple(cpuResource    / deno.cpuResource, 
								   memoryResource / deno.memoryResource, 
								  networkResource / deno.networkResource);
	}
	
	public double infiniNorm()
	{
		return   Math.max(cpuResource, Math.max(memoryResource, networkResource));
	}
	
	
	public boolean isAllNonZero()
	{
		return cpuResource > 0 && memoryResource > 0 && networkResource > 0;
		
	}
	
	
	
	
}
