/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivityFactory;
import jsprit.core.problem.vehicle.PenaltyVehicleType;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Resource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class VrpXMLReader{
	
	public interface ServiceBuilderFactory {
		Service.Builder createBuilder(String serviceType, String id, Integer size);
	}
	
	static class DefaultServiceBuilderFactory implements ServiceBuilderFactory{

		@Override
		public jsprit.core.problem.job.Service.Builder createBuilder(String serviceType, String id, Integer size) {
			if(serviceType.equals("pickup")){
				if(size != null) return Pickup.Builder.newInstance(id).addSizeDimension(0, size);
				else return Pickup.Builder.newInstance(id);
			}
			else if(serviceType.equals("delivery")){
				if(size != null) return Delivery.Builder.newInstance(id).addSizeDimension(0, size);
				else return Delivery.Builder.newInstance(id);
			}
			else{
				if(size != null) return Service.Builder.newInstance(id).addSizeDimension(0, size);
				else return Service.Builder.newInstance(id);
				
			}
		}
	}
	
	interface JobConfigReader {
		
		void readConfig(XMLConfiguration vrpProblem);
	}
	
	static class ServiceConfigReader implements JobConfigReader{
		
		VehicleRoutingProblem.Builder vrpBuilder;
		
		public ServiceConfigReader(jsprit.core.problem.VehicleRoutingProblem.Builder vrpBuilder) {
			super();
			this.vrpBuilder = vrpBuilder;
		}

		@Override
		public void readConfig(XMLConfiguration config) {
			
		}
		
	}
	
	
	static class ShipmentConfigReader implements JobConfigReader{

		VehicleRoutingProblem.Builder vrpBuilder;
		
		public ShipmentConfigReader(jsprit.core.problem.VehicleRoutingProblem.Builder vrpBuilder) {
			super();
			this.vrpBuilder = vrpBuilder;
		}

		@Override
		public void readConfig(XMLConfiguration config) {
			
		}
		
	}
	

	private static Logger logger = Logger.getLogger(VrpXMLReader.class);
	
	private VehicleRoutingProblem.Builder vrpBuilder;
	
	private Map<String,Vehicle> vehicleMap;

	private Map<String, Service> serviceMap; 
	
	private Map<String, Shipment> shipmentMap;
	
	private Set<String> freezedJobIds = new HashSet<String>();
	
	private boolean schemaValidation = true;

	private Collection<VehicleRoutingProblemSolution> solutions;
	
	private ServiceBuilderFactory serviceBuilderFactory = new DefaultServiceBuilderFactory();
	
	private Collection<JobConfigReader> jobConfigReaders = new ArrayList<VrpXMLReader.JobConfigReader>();

	
	public void addJobConfigReader(JobConfigReader reader){
		jobConfigReaders.add(reader);
	}
	public void setTourActivityFactory(TourActivityFactory tourActivityFactory){
	}
	
	public void setServiceBuilderFactory(ServiceBuilderFactory serviceBuilderFactory){
		this.serviceBuilderFactory=serviceBuilderFactory;
	}
	
	/**
	 * @param schemaValidation the schemaValidation to set
	 */
	public void setSchemaValidation(boolean schemaValidation) {
		this.schemaValidation = schemaValidation;
	}

	public VrpXMLReader(VehicleRoutingProblem.Builder vrpBuilder, Collection<VehicleRoutingProblemSolution> solutions){
		this.vrpBuilder = vrpBuilder;
		this.vehicleMap = new HashMap<String, Vehicle>();
		this.serviceMap = new HashMap<String, Service>();
		this.shipmentMap = new HashMap<String, Shipment>();
		this.solutions = solutions;
	}
	
	public VrpXMLReader(VehicleRoutingProblem.Builder vrpBuilder){
		this.vrpBuilder = vrpBuilder;
		this.vehicleMap = new HashMap<String, Vehicle>();
		this.serviceMap = new HashMap<String, Service>();
		this.shipmentMap = new HashMap<String, Shipment>();
		this.solutions = null;
	}
	
	public void read(String filename) {
		logger.info("read vrp from file " + filename);
		XMLConfiguration xmlConfig = new XMLConfiguration();
		xmlConfig.setFileName(filename);
		xmlConfig.setAttributeSplittingDisabled(true);
		xmlConfig.setDelimiterParsingDisabled(true);
		
		if(schemaValidation){
			final InputStream resource = Resource.getAsInputStream("vrp_xml_schema.xsd");
			if(resource != null) {
				EntityResolver resolver = new EntityResolver() {

					@Override
					public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
						{
							InputSource is = new InputSource(resource);
							return is;
						}
					}
				};
				xmlConfig.setEntityResolver(resolver);
				xmlConfig.setSchemaValidation(true);
				logger.info("validating " + filename + " with xsd-schema");
			}
			else{
				logger.warn("cannot find schema-xsd file (vrp_xml_schema.xsd). try to read xml without xml-file-validation.");
			}		
		}
		try {
			xmlConfig.load();
		} catch (ConfigurationException e) {
			logger.error(e);
			e.printStackTrace();
			System.exit(1);
		}
		readProblemType(xmlConfig);
		readVehiclesAndTheirTypes(xmlConfig);
		
		readShipments(xmlConfig);
		readServices(xmlConfig);
		
		readInitialRoutes(xmlConfig);
		readSolutions(xmlConfig);
		
		addJobsAndTheirLocationsToVrp();
	}

	private void addJobsAndTheirLocationsToVrp() {
		for(Service service : serviceMap.values()) {
			if(!freezedJobIds.contains(service.getId())){
				vrpBuilder.addJob(service);
			}
		}
		for(Shipment shipment : shipmentMap.values()){ 
			if(!freezedJobIds.contains(shipment.getId())){
				vrpBuilder.addJob(shipment);
			}
		}
	}
	private void readInitialRoutes(XMLConfiguration xmlConfig) {
		List<HierarchicalConfiguration> initialRouteConfigs = xmlConfig.configurationsAt("initialRoutes.route");
		for(HierarchicalConfiguration routeConfig : initialRouteConfigs){
			Driver driver = DriverImpl.noDriver();
			String vehicleId = routeConfig.getString("vehicleId");
			Vehicle vehicle = getVehicle(vehicleId);
			if(vehicle == null) throw new IllegalStateException("vehicle is missing.");
			String start = routeConfig.getString("start");
			if(start == null) throw new IllegalStateException("route start-time is missing.");
			double departureTime = Double.parseDouble(start);
			
			VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
			routeBuilder.setDepartureTime(departureTime);
			
			List<HierarchicalConfiguration> actConfigs = routeConfig.configurationsAt("act");
			for(HierarchicalConfiguration actConfig : actConfigs){
				String type = actConfig.getString("[@type]");
				if(type == null) throw new IllegalStateException("act[@type] is missing.");
				double arrTime = 0.;
				double endTime = 0.;
				String arrTimeS = actConfig.getString("arrTime");
				if(arrTimeS!=null) arrTime=Double.parseDouble(arrTimeS);
				String endTimeS = actConfig.getString("endTime");
				if(endTimeS!=null) endTime=Double.parseDouble(endTimeS);
				
				String serviceId = actConfig.getString("serviceId");
				if(serviceId != null) {
					Service service = getService(serviceId);
					if(service==null) throw new IllegalStateException("service to serviceId " + serviceId + " is missing (reference in one of your initial routes). make sure you define the service you refer to here in <services> </services>.");
					//!!!since job is part of initial route, it does not belong to jobs in problem, i.e. variable jobs that can be assigned/scheduled
					freezedJobIds.add(serviceId);
					routeBuilder.addService(service, arrTime, endTime);
				}
				else{
					String shipmentId = actConfig.getString("shipmentId");
					if(shipmentId == null) throw new IllegalStateException("either serviceId or shipmentId is missing");
					Shipment shipment = getShipment(shipmentId);
					if(shipment == null) throw new IllegalStateException("shipment to shipmentId " + shipmentId + " is missing (reference in one of your initial routes). make sure you define the shipment you refer to here in <shipments> </shipments>.");
					freezedJobIds.add(shipmentId);
					if(type.equals("pickupShipment")){
						routeBuilder.addPickup(shipment, arrTime, endTime);
					}
					else if(type.equals("deliverShipment")){
						routeBuilder.addDelivery(shipment, arrTime, endTime);
					}
					else throw new IllegalStateException("type " + type + " is not supported. Use 'pickupShipment' or 'deliverShipment' here");
				}
			}
			VehicleRoute route = routeBuilder.build();
			vrpBuilder.addInitialVehicleRoute(route);
		}
		
	}
	private void readSolutions(XMLConfiguration vrpProblem) {
		if(solutions == null) return;
		List<HierarchicalConfiguration> solutionConfigs = vrpProblem.configurationsAt("solutions.solution");
		for(HierarchicalConfiguration solutionConfig : solutionConfigs){
			String totalCost = solutionConfig.getString("cost");
			double cost = -1;
			if(totalCost != null) cost = Double.parseDouble(totalCost);
			List<HierarchicalConfiguration> routeConfigs = solutionConfig.configurationsAt("routes.route");
			List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
			for(HierarchicalConfiguration routeConfig : routeConfigs){
				//! here, driverId is set to noDriver, no matter whats in driverId.
				Driver driver = DriverImpl.noDriver();
				String vehicleId = routeConfig.getString("vehicleId");
				Vehicle vehicle = getVehicle(vehicleId);
				if(vehicle == null) throw new IllegalStateException("vehicle is missing.");
				String start = routeConfig.getString("start");
				if(start == null) throw new IllegalStateException("route start-time is missing.");
				double departureTime = Double.parseDouble(start);
				
				String end = routeConfig.getString("end");
				if(end == null) throw new IllegalStateException("route end-time is missing.");
				
				VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
				routeBuilder.setDepartureTime(departureTime);
				routeBuilder.setRouteEndArrivalTime(Double.parseDouble(end));
				List<HierarchicalConfiguration> actConfigs = routeConfig.configurationsAt("act");
				for(HierarchicalConfiguration actConfig : actConfigs){
					String type = actConfig.getString("[@type]");
					if(type == null) throw new IllegalStateException("act[@type] is missing.");
					double arrTime = 0.;
					double endTime = 0.;
					String arrTimeS = actConfig.getString("arrTime");
					if(arrTimeS!=null) arrTime=Double.parseDouble(arrTimeS);
					String endTimeS = actConfig.getString("endTime");
					if(endTimeS!=null) endTime=Double.parseDouble(endTimeS);
					
					String serviceId = actConfig.getString("serviceId");
					if(serviceId != null) {
						Service service = getService(serviceId);
						routeBuilder.addService(service, arrTime, endTime);
					}
					else{
						String shipmentId = actConfig.getString("shipmentId");
						if(shipmentId == null) throw new IllegalStateException("either serviceId or shipmentId is missing");
						Shipment shipment = getShipment(shipmentId);
						if(shipment == null) throw new IllegalStateException("shipment with id " + shipmentId + " does not exist.");
						if(type.equals("pickupShipment")){
							routeBuilder.addPickup(shipment, arrTime, endTime);
						}
						else if(type.equals("deliverShipment")){
							routeBuilder.addDelivery(shipment, arrTime, endTime);
						}
						else throw new IllegalStateException("type " + type + " is not supported. Use 'pickupShipment' or 'deliverShipment' here");
					}	
				}
				routes.add(routeBuilder.build());
			}
			VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, cost);
			solutions.add(solution);
		}
	}
	
	private Shipment getShipment(String shipmentId) {
		return shipmentMap.get(shipmentId);
	}
	private Service getService(String serviceId) {
		return serviceMap.get(serviceId);
	}

	private Vehicle getVehicle(String vehicleId) {
		return vehicleMap.get(vehicleId);
	}

	private void readProblemType(XMLConfiguration vrpProblem) {
		String fleetSize = vrpProblem.getString("problemType.fleetSize");
		if(fleetSize == null) vrpBuilder.setFleetSize(FleetSize.INFINITE);
		else if(fleetSize.toUpperCase().equals(FleetSize.INFINITE.toString())) vrpBuilder.setFleetSize(FleetSize.INFINITE);
		else vrpBuilder.setFleetSize(FleetSize.FINITE);
	}
	
	private void readShipments(XMLConfiguration config){
		List<HierarchicalConfiguration> shipmentConfigs = config.configurationsAt("shipments.shipment");
		for(HierarchicalConfiguration shipmentConfig : shipmentConfigs){
			String id = shipmentConfig.getString("[@id]");
			if(id == null) throw new IllegalStateException("shipment[@id] is missing.");
			
			String capacityString = shipmentConfig.getString("capacity-demand");
			boolean capacityDimensionsExist = shipmentConfig.containsKey("capacity-dimensions.dimension(0)");
			if(capacityString == null && !capacityDimensionsExist){ 
				throw new IllegalStateException("capacity of shipment is not set. use 'capacity-dimensions'"); 
			}
			if(capacityString != null && capacityDimensionsExist){
				throw new IllegalStateException("either use capacity or capacity-dimension, not both. prefer the use of 'capacity-dimensions' over 'capacity'.");
			}
			
			Shipment.Builder builder;
			if(capacityString != null){
				builder = Shipment.Builder.newInstance(id).addSizeDimension(0, Integer.parseInt(capacityString));
			}
			else {
				builder = Shipment.Builder.newInstance(id);
				List<HierarchicalConfiguration> dimensionConfigs = shipmentConfig.configurationsAt("capacity-dimensions.dimension");
				for(HierarchicalConfiguration dimension : dimensionConfigs){
					Integer index = dimension.getInt("[@index]");
					Integer value = dimension.getInt("");
					builder.addSizeDimension(index, value);
				}
			}
			
			//pickup-locationId
			String pickupLocationId = shipmentConfig.getString("pickup.locationId");
			if(pickupLocationId != null){
				builder.setPickupLocation(pickupLocationId);
			}
			
			//pickup-coord
			Coordinate pickupCoord = getCoord(shipmentConfig,"pickup.");
			if(pickupCoord != null){
				builder.setPickupCoord(pickupCoord);
				if(pickupLocationId != null){
//					vrpBuilder.addLocation(pickupLocationId,pickupCoord);
				}
				else{
//					vrpBuilder.addLocation(pickupCoord.toString(),pickupCoord);
					builder.setPickupLocation(pickupCoord.toString());
				}
			}
			//pickup-serviceTime
			String pickupServiceTime = shipmentConfig.getString("pickup.duration");
			if(pickupServiceTime != null) builder.setPickupServiceTime(Double.parseDouble(pickupServiceTime));
			
			//pickup-tw
			String pickupTWStart = shipmentConfig.getString("pickup.timeWindows.timeWindow(0).start");
			String pickupTWEnd = shipmentConfig.getString("pickup.timeWindows.timeWindow(0).end");
			if(pickupTWStart != null && pickupTWEnd != null){
				TimeWindow pickupTW = TimeWindow.newInstance(Double.parseDouble(pickupTWStart), Double.parseDouble(pickupTWEnd));
				builder.setPickupTimeWindow(pickupTW);
			}
			
			
			//delivery-locationId
			String deliveryLocationId = shipmentConfig.getString("delivery.locationId");
			if(deliveryLocationId != null){
				builder.setDeliveryLocation(deliveryLocationId);
			}
			
			//delivery-coord
			Coordinate deliveryCoord = getCoord(shipmentConfig,"delivery.");
			if(deliveryCoord != null){
				builder.setDeliveryCoord(deliveryCoord);
				if(deliveryLocationId != null){
//					vrpBuilder.addLocation(deliveryLocationId,deliveryCoord);
				}
				else{
//					vrpBuilder.addLocation(deliveryCoord.toString(),deliveryCoord);
					builder.setDeliveryLocation(deliveryCoord.toString());
				}
			}
			//delivery-serviceTime
			String deliveryServiceTime = shipmentConfig.getString("delivery.duration");
			if(deliveryServiceTime != null) builder.setDeliveryServiceTime(Double.parseDouble(deliveryServiceTime));
			
			//delivery-tw
			String delTWStart = shipmentConfig.getString("delivery.timeWindows.timeWindow(0).start");
			String delTWEnd = shipmentConfig.getString("delivery.timeWindows.timeWindow(0).end");
			if(delTWStart != null && delTWEnd != null){
				TimeWindow delTW = TimeWindow.newInstance(Double.parseDouble(delTWStart), Double.parseDouble(delTWEnd));
				builder.setDeliveryTimeWindow(delTW);
			}
			
			
			Shipment shipment = builder.build();
//			vrpBuilder.addJob(shipment);
			shipmentMap.put(shipment.getId(),shipment);
		}
	}

	private static Coordinate getCoord(HierarchicalConfiguration serviceConfig, String prefix) {
		Coordinate pickupCoord = null;
		if(serviceConfig.getString(prefix + "coord[@x]") != null && serviceConfig.getString(prefix + "coord[@y]") != null){
			double x = Double.parseDouble(serviceConfig.getString(prefix + "coord[@x]"));
			double y = Double.parseDouble(serviceConfig.getString(prefix + "coord[@y]"));
			pickupCoord = Coordinate.newInstance(x,y);
		}
		return pickupCoord;
	}

	private void readServices(XMLConfiguration vrpProblem) {
		List<HierarchicalConfiguration> serviceConfigs = vrpProblem.configurationsAt("services.service");
		for(HierarchicalConfiguration serviceConfig : serviceConfigs){
			String id = serviceConfig.getString("[@id]");
			if(id == null) throw new IllegalStateException("service[@id] is missing.");
			String type = serviceConfig.getString("[@type]");
			if(type == null) type = "service";
			
			String capacityString = serviceConfig.getString("capacity-demand");
			boolean capacityDimensionsExist = serviceConfig.containsKey("capacity-dimensions.dimension(0)");
			if(capacityString == null && !capacityDimensionsExist){ 
				throw new IllegalStateException("capacity of service is not set. use 'capacity-dimensions'"); 
			}
			if(capacityString != null && capacityDimensionsExist){
				throw new IllegalStateException("either use capacity or capacity-dimension, not both. prefer the use of 'capacity-dimensions' over 'capacity'.");
			}
			
			Service.Builder builder;
			if(capacityString != null){
				builder = serviceBuilderFactory.createBuilder(type, id, Integer.parseInt(capacityString));
			}
			else {
				builder = serviceBuilderFactory.createBuilder(type, id, null);
				List<HierarchicalConfiguration> dimensionConfigs = serviceConfig.configurationsAt("capacity-dimensions.dimension");
				for(HierarchicalConfiguration dimension : dimensionConfigs){
					Integer index = dimension.getInt("[@index]");
					Integer value = dimension.getInt("");
					builder.addSizeDimension(index, value);
				}
			}
			String serviceLocationId = serviceConfig.getString("locationId");
			if(serviceLocationId != null) builder.setLocationId(serviceLocationId);
			Coordinate serviceCoord = getCoord(serviceConfig,"");
			if(serviceCoord != null){
				builder.setCoord(serviceCoord);
				if(serviceLocationId != null){
//					vrpBuilder.addLocation(serviceLocationId,serviceCoord);
				}
				else{
//					vrpBuilder.addLocation(serviceCoord.toString(),serviceCoord);
					builder.setLocationId(serviceCoord.toString());
				}
			}
			if(serviceConfig.containsKey("duration")){
				builder.setServiceTime(serviceConfig.getDouble("duration"));
			}
			List<HierarchicalConfiguration> deliveryTWConfigs = serviceConfig.configurationsAt("timeWindows.timeWindow");
			if(!deliveryTWConfigs.isEmpty()){
				for(HierarchicalConfiguration twConfig : deliveryTWConfigs){
					builder.setTimeWindow(TimeWindow.newInstance(twConfig.getDouble("start"), twConfig.getDouble("end")));
				}
			}
			Service service = builder.build();
			serviceMap.put(service.getId(),service);
//			vrpBuilder.addJob(service);

		}
	}

	private void readVehiclesAndTheirTypes(XMLConfiguration vrpProblem) {

		//read vehicle-types
		Map<String, VehicleType> types = new HashMap<String, VehicleType>();
		List<HierarchicalConfiguration> typeConfigs = vrpProblem.configurationsAt("vehicleTypes.type");
		for(HierarchicalConfiguration typeConfig : typeConfigs){
			String typeId = typeConfig.getString("id");
			if(typeId == null) throw new IllegalStateException("typeId is missing.");
			
			String capacityString = typeConfig.getString("capacity");
			boolean capacityDimensionsExist = typeConfig.containsKey("capacity-dimensions.dimension(0)");
			if(capacityString == null && !capacityDimensionsExist){ 
				throw new IllegalStateException("capacity of type is not set. use 'capacity-dimensions'"); 
			}
			if(capacityString != null && capacityDimensionsExist){
				throw new IllegalStateException("either use capacity or capacity-dimension, not both. prefer the use of 'capacity-dimensions' over 'capacity'.");
			}
			
			VehicleTypeImpl.Builder typeBuilder;
			if(capacityString != null){
				typeBuilder = VehicleTypeImpl.Builder.newInstance(typeId).addCapacityDimension(0, Integer.parseInt(capacityString));
			}
			else {
				typeBuilder = VehicleTypeImpl.Builder.newInstance(typeId);
				List<HierarchicalConfiguration> dimensionConfigs = typeConfig.configurationsAt("capacity-dimensions.dimension");
				for(HierarchicalConfiguration dimension : dimensionConfigs){
					Integer index = dimension.getInt("[@index]");
					Integer value = dimension.getInt("");
					typeBuilder.addCapacityDimension(index, value);
				}
			}
			Double fix = typeConfig.getDouble("costs.fixed");
			Double timeC = typeConfig.getDouble("costs.time");
			Double distC = typeConfig.getDouble("costs.distance");
			
			if(fix != null) typeBuilder.setFixedCost(fix);
			if(timeC != null) typeBuilder.setCostPerTime(timeC);
			if(distC != null) typeBuilder.setCostPerDistance(distC);
			VehicleType type = typeBuilder.build();
			String id = type.getTypeId();
			String penalty = typeConfig.getString("[@type]");
			if(penalty != null){
				if(penalty.equals("penalty")){
					String penaltyFactor = typeConfig.getString("[@penaltyFactor]");
					if(penaltyFactor != null){
						type = new PenaltyVehicleType(type,Double.parseDouble(penaltyFactor));
					}
					else type = new PenaltyVehicleType(type);
					id = id + "_penalty";
				}
			}
			
			types.put(id, type);
		}
		
		//read vehicles
		List<HierarchicalConfiguration> vehicleConfigs = vrpProblem.configurationsAt("vehicles.vehicle");
		boolean doNotWarnAgain = false;
		for(HierarchicalConfiguration vehicleConfig : vehicleConfigs){
			String vehicleId = vehicleConfig.getString("id");
			if(vehicleId == null) throw new IllegalStateException("vehicleId is missing.");
			Builder builder = VehicleImpl.Builder.newInstance(vehicleId);
			String typeId = vehicleConfig.getString("typeId");
			if(typeId == null) throw new IllegalStateException("typeId is missing.");
			String vType = vehicleConfig.getString("[@type]");
			if(vType!=null){
				if(vType.equals("penalty")){
					typeId+="_penalty";
				}
			}
			VehicleType type = types.get(typeId);
			if(type == null) throw new IllegalStateException("vehicleType with typeId " + typeId + " is missing.");
			builder.setType(type);
			String locationId = vehicleConfig.getString("location.id");
			if(locationId == null) {
				locationId = vehicleConfig.getString("startLocation.id");
			}
			if(locationId == null) throw new IllegalStateException("location.id is missing.");
			builder.setStartLocationId(locationId);
			String coordX = vehicleConfig.getString("location.coord[@x]");
			String coordY = vehicleConfig.getString("location.coord[@y]");
			if(coordX == null || coordY == null) {
				coordX = vehicleConfig.getString("startLocation.coord[@x]");
				coordY = vehicleConfig.getString("startLocation.coord[@y]");
			}
			if(coordX == null || coordY == null) {
					if(!doNotWarnAgain) {
						logger.warn("location.coord is missing. will not warn you again.");
						doNotWarnAgain = true;
					}
			}
			else{
				Coordinate coordinate = Coordinate.newInstance(Double.parseDouble(coordX), Double.parseDouble(coordY));
				builder.setStartLocationCoordinate(coordinate);
				
			}
			
			String endLocationId = vehicleConfig.getString("endLocation.id");
			if(endLocationId != null) builder.setEndLocationId(endLocationId);
			String endCoordX = vehicleConfig.getString("endLocation.coord[@x]");
			String endCoordY = vehicleConfig.getString("endLocation.coord[@y]");
			if(endCoordX == null || endCoordY == null) {
				if(!doNotWarnAgain) {
					logger.warn("endLocation.coord is missing. will not warn you again.");
					doNotWarnAgain = true;
				}
			}
			else{
				Coordinate coordinate = Coordinate.newInstance(Double.parseDouble(endCoordX), Double.parseDouble(endCoordY));
				builder.setEndLocationCoordinate(coordinate);
			}
			
			
			String start = vehicleConfig.getString("timeSchedule.start");
			String end = vehicleConfig.getString("timeSchedule.end");
			if(start != null) builder.setEarliestStart(Double.parseDouble(start));
			if(end != null) builder.setLatestArrival(Double.parseDouble(end));
			String returnToDepot = vehicleConfig.getString("returnToDepot");
			if(returnToDepot != null){
				builder.setReturnToDepot(vehicleConfig.getBoolean("returnToDepot"));
			}
			VehicleImpl vehicle = builder.build();
			vrpBuilder.addVehicle(vehicle);
			vehicleMap.put(vehicleId, vehicle);
		}

	}
	
	

}
