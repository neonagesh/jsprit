package jsprit.core.problem.job;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.util.Coordinate;

/**
 * Shipment is an implementation of Job and consists of a pickup and a delivery of something.
 * 
 * <p>It distinguishes itself from {@link Service} as two locations are involved a pickup where usually 
 * something is loaded to the transport unit and a delivery where something is unloaded.
 * 
 * <p>By default serviceTimes of both pickup and delivery is 0.0 and timeWindows of both is [0.0, Double.MAX_VALUE],
 * 
 * <p>A shipment can be built with a builder. You can get an instance of the builder by coding <code>Shipment.Builder.newInstance(...)</code>.
 * This way you can specify the shipment. Once you build the shipment, it is immutable, i.e. fields/attributes cannot be changed anymore and 
 * you can only 'get' the specified values.
 * 
 * <p>Note that two shipments are equal if they have the same id.
 * 
 * @author schroeder
 *
 */
public class Shipment implements Job{

	/**
	 * Builder that builds the shipment.
	 * 
	 * @author schroeder
	 *
	 */
	public static class Builder {
		
		private String id;
		
		private String pickupLocation;
		
		private Coordinate pickupCoord;
		
		private double pickupServiceTime = 0.0;
		
		private String deliveryLocation;
		
		private Coordinate deliveryCoord;

		private double deliveryServiceTime = 0.0;

		private TimeWindow deliveryTimeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);

		private TimeWindow pickupTimeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);
		
		private Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();
		
		private Capacity capacity;
		
		/**
		 * Returns new instance of this builder.
		 * 
		 * @param id
		 * @return
		 */
		public static Builder newInstance(String id){
			return new Builder(id);
		}
		
		/**
		 * Constructs the builder
		 * 
		 * @param id
		 * @param size
		 * @throws IllegalArgumentException if size < 0 or id is null
		 */
		Builder(String id, int size) {
			if(size < 0) throw new IllegalArgumentException("size must be greater than or equal to zero");
			if(id == null) throw new IllegalArgumentException("id must not be null");
			this.id = id;
		}
		
		Builder(String id){
			if(id == null) throw new IllegalArgumentException("id must not be null");
			this.id = id;
		}
		
		/**
		 * Sets pickup-location.
		 * 
		 * @param pickupLocation
		 * @return builder
		 * @throws IllegalArgumentException if location is null
		 */
		public Builder setPickupLocation(String pickupLocation){
			if(pickupLocation == null) throw new IllegalArgumentException("location must not be null");
			this.pickupLocation = pickupLocation;
			return this;
		}
		
		/**
		 * Sets pickup-coordinate.
		 * 
		 * @param pickupCoord
		 * @return builder
		 * @throws IllegalArgumentException if pickupCoord is null
		 */
		public Builder setPickupCoord(Coordinate pickupCoord){
			if(pickupCoord == null) throw new IllegalArgumentException("coord must not be null");
			this.pickupCoord = pickupCoord;
			return this;
		}
		
		/**
		 * Sets pickupServiceTime.
		 * 
		 * <p>ServiceTime is intended to be the time the implied activity takes at the pickup-location.
		 * 
		 * @param serviceTime
		 * @return builder
		 * @throws IllegalArgumentException if servicTime < 0.0
		 */
		public Builder setPickupServiceTime(double serviceTime){
			if(serviceTime < 0.0) throw new IllegalArgumentException("serviceTime must not be < 0.0");
			this.pickupServiceTime = serviceTime;
			return this;
		}
		
		/**
		 * Sets the timeWindow for the pickup, i.e. the time-period in which a pickup operation is
		 * allowed to start.
		 * 
		 * <p>By default timeWindow is [0.0, Double.MAX_VALUE}
		 * 
		 * @param timeWindow
		 * @return builder
		 * @throws IllegalArgumentException if timeWindow is null
		 */
		public Builder setPickupTimeWindow(TimeWindow timeWindow){
			if(timeWindow == null) throw new IllegalArgumentException("timeWindow cannot be null");
			this.pickupTimeWindow = timeWindow;
			return this;
		}
			
		/**
		 * Sets the delivery-location.
		 * 
		 * @param deliveryLocation
		 * @return builder
		 * @throws IllegalArgumentException if location is null
		 */
		public Builder setDeliveryLocation(String deliveryLocation){
			if(deliveryLocation == null) throw new IllegalArgumentException("delivery location must not be null");
			this.deliveryLocation = deliveryLocation;
			return this;
		}
		
		/**
		 * Sets delivery-coord.
		 * 
		 * @param deliveryCoord
		 * @return builder
		 * @throws IllegalArgumentException if coord is null;
		 */
		public Builder setDeliveryCoord(Coordinate deliveryCoord){
			if(deliveryCoord == null) throw new IllegalArgumentException("coord must not be null");
			this.deliveryCoord = deliveryCoord;
			return this;
		}
		
		/**
		 * Sets the delivery service-time.
		 * 
		 * <p>ServiceTime is intended to be the time the implied activity takes at the delivery-location.
		 * 
		 * @param deliveryServiceTime
		 * @return builder
		 * @throws IllegalArgumentException if serviceTime < 0.0
		 */
		public Builder setDeliveryServiceTime(double deliveryServiceTime){
			if(deliveryServiceTime < 0.0) throw new IllegalArgumentException("deliveryServiceTime must not be < 0.0");
			this.deliveryServiceTime = deliveryServiceTime;
			return this;
		}
		
		/**
		 * Sets the timeWindow for the delivery, i.e. the time-period in which a delivery operation is
		 * allowed to start.
		 * 
		 * <p>By default timeWindow is [0.0, Double.MAX_VALUE}
		 * 
		 * @param timeWindow
		 * @return builder
		 * @throws IllegalArgumentException if timeWindow is null
		 */
		public Builder setDeliveryTimeWindow(TimeWindow timeWindow){
			if(timeWindow == null) throw new IllegalArgumentException("delivery time-window must not be null");
			this.deliveryTimeWindow = timeWindow;
			return this;
		}
		
		/**
		 * Adds capacity dimension.
		 * 
		 * @param dimensionIndex
		 * @param dimensionValue
		 * @return builder
		 * @throws IllegalArgumentException if dimVal < 0
		 */
		public Builder addSizeDimension(int dimensionIndex, int dimensionValue) {
			if(dimensionValue<0) throw new IllegalArgumentException("capacity value cannot be negative");
			capacityBuilder.addDimension(dimensionIndex, dimensionValue);
			return this;
		}
		

		/**
		 * Builds the shipment.
		 * 
		 * @return shipment
		 * @throws IllegalStateException if neither pickup-location nor pickup-coord is set or if neither delivery-location nor delivery-coord
		 * is set
		 */
		public Shipment build(){
			if(pickupLocation == null) { 
				if(pickupCoord == null) throw new IllegalStateException("either locationId or a coordinate must be given. But is not.");
				pickupLocation = pickupCoord.toString();
			}
			if(deliveryLocation == null) { 
				if(deliveryCoord == null) throw new IllegalStateException("either locationId or a coordinate must be given. But is not.");
				deliveryLocation = deliveryCoord.toString();
			}
			capacity = capacityBuilder.build();
			return new Shipment(this);
		}

		
	}
	
	private final String id;
	
	private final String pickupLocation;
	
	private final Coordinate pickupCoord;
	
	private final double pickupServiceTime;
	
	private final String deliveryLocation;
	
	private final Coordinate deliveryCoord;

	private final double deliveryServiceTime;

	private final TimeWindow deliveryTimeWindow;

	private final TimeWindow pickupTimeWindow;
	
	private final Capacity capacity;

	
	/**
	 * Constructs the shipment.
	 * 
	 * @param builder
	 */
	Shipment(Builder builder){
		this.id = builder.id;
		this.pickupLocation = builder.pickupLocation;
		this.pickupCoord = builder.pickupCoord;
		this.pickupServiceTime = builder.pickupServiceTime;
		this.pickupTimeWindow = builder.pickupTimeWindow;
		this.deliveryLocation = builder.deliveryLocation;
		this.deliveryCoord = builder.deliveryCoord;
		this.deliveryServiceTime = builder.deliveryServiceTime;
		this.deliveryTimeWindow = builder.deliveryTimeWindow;
		this.capacity = builder.capacity;
	}
	
	@Override
	public String getId() {
		return id;
	}

	/**
	 * Returns the pickup-location.
	 * 
	 * @return pickup-location
	 */
	public String getPickupLocation() {
		return pickupLocation;
	}

	/**
	 * Returns the pickup-coordinate.
	 * 
	 * @return coordinate of the pickup
	 */
	public Coordinate getPickupCoord() {
		return pickupCoord;
	}

	/**
	 * Returns the pickup service-time.
	 * 
	 * <p>By default service-time is 0.0.
	 * 
	 * @return service-time
	 */
	public double getPickupServiceTime() {
		return pickupServiceTime;
	}

	/**
	 * Returns delivery-location.
	 * 
	 * @return delivery-location
	 */
	public String getDeliveryLocation() {
		return deliveryLocation;
	}

	/**
	 * Returns coordinate of the delivery.
	 * 
	 * @return coordinate of delivery
	 */
	public Coordinate getDeliveryCoord() {
		return deliveryCoord;
	}

	/**
	 * Returns service-time of delivery.
	 * 
	 * @return service-time of delivery
	 */
	public double getDeliveryServiceTime() {
		return deliveryServiceTime;
	}

	/**
	 * Returns the time-window of delivery.
	 * 
	 * @return time-window of delivery
	 */
	public TimeWindow getDeliveryTimeWindow() {
		return deliveryTimeWindow;
	}

	/**
	 * Returns the time-window of pickup.
	 * 
	 * @return time-window of pickup
	 */
	public TimeWindow getPickupTimeWindow() {
		return pickupTimeWindow;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Two shipments are equal if they have the same id.
	 * 
	 * @return true if shipments are equal (have the same id)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Shipment other = (Shipment) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public Capacity getSize() {
		return capacity;
	}

	
}
