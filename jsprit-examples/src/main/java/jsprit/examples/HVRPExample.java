package jsprit.examples;

import java.util.Collection;

import jsprit.analysis.toolbox.GraphStreamViewer;
import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.analysis.toolbox.SolutionPrinter.Print;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;

/**
 * customers (id,x,y,demand)
 * 1 22 22 18 
 * 2 36 26 26 
 * 3 21 45 11 
 * 4 45 35 30 
 * 5 55 20 21 
 * 6 33 34 19 
 * 7 50 50 15 
 * 8 55 45 16 
 * 9 26 59 29 
 * 10 40 66 26
 * 11 55 65 37 
 * 12 35 51 16 
 * 13 62 35 12 
 * 14 62 57 31 
 * 15 62 24 8 
 * 16 21 36 19 
 * 17 33 44 20 
 * 18 9 56 13 
 * 19 62 48 15 
 * 20 66 14 22
 * 
 * vehicles (id,cap,fixed costs, perDistance, #vehicles) at location (40,40)
 * 1 120 1000 1.0 2
 * 2 160 1500 1.1 1
 * 3 300 3500 1.4 1
 *  
 * @author schroeder
 *
 */
public class HVRPExample {
	
	
	public static void main(String[] args) {
		
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		
		//add customers
		vrpBuilder.addJob(Service.Builder.newInstance("1").addSizeDimension(0, 18).setCoord(Coordinate.newInstance(22, 22)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("2").addSizeDimension(0, 26).setCoord(Coordinate.newInstance(36, 26)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("3").addSizeDimension(0, 11).setCoord(Coordinate.newInstance(21, 45)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("4").addSizeDimension(0, 30).setCoord(Coordinate.newInstance(45, 35)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("5").addSizeDimension(0, 21).setCoord(Coordinate.newInstance(55, 20)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("6").addSizeDimension(0, 19).setCoord(Coordinate.newInstance(33, 34)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("7").addSizeDimension(0, 15).setCoord(Coordinate.newInstance(50, 50)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("8").addSizeDimension(0, 16).setCoord(Coordinate.newInstance(55, 45)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("9").addSizeDimension(0, 29).setCoord(Coordinate.newInstance(26, 59)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("10").addSizeDimension(0, 26).setCoord(Coordinate.newInstance(40, 66)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("11").addSizeDimension(0, 37).setCoord(Coordinate.newInstance(55, 56)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("12").addSizeDimension(0, 16).setCoord(Coordinate.newInstance(35, 51)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("13").addSizeDimension(0, 12).setCoord(Coordinate.newInstance(62, 35)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("14").addSizeDimension(0, 31).setCoord(Coordinate.newInstance(62, 57)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("15").addSizeDimension(0, 8).setCoord(Coordinate.newInstance(62, 24)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("16").addSizeDimension(0, 19).setCoord(Coordinate.newInstance(21, 36)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("17").addSizeDimension(0, 20).setCoord(Coordinate.newInstance(33, 44)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("18").addSizeDimension(0, 13).setCoord(Coordinate.newInstance(9, 56)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("19").addSizeDimension(0, 15).setCoord(Coordinate.newInstance(62, 48)).build());
		vrpBuilder.addJob(Service.Builder.newInstance("20").addSizeDimension(0, 22).setCoord(Coordinate.newInstance(66, 14)).build());
		
		
		//add vehicle - finite fleet
		//2xtype1
		VehicleType type1 = VehicleTypeImpl.Builder.newInstance("type_1").addCapacityDimension(0, 120).setCostPerDistance(1.0).build();
		VehicleImpl vehicle1_1 = VehicleImpl.Builder.newInstance("1_1").setStartLocationCoordinate(Coordinate.newInstance(40, 40)).setType(type1).build();
		vrpBuilder.addVehicle(vehicle1_1);
		VehicleImpl vehicle1_2 = VehicleImpl.Builder.newInstance("1_2").setStartLocationCoordinate(Coordinate.newInstance(40, 40)).setType(type1).build();
		vrpBuilder.addVehicle(vehicle1_2);
		//1xtype2
		VehicleType type2 = VehicleTypeImpl.Builder.newInstance("type_2").addCapacityDimension(0, 160).setCostPerDistance(1.1).build();
		VehicleImpl vehicle2_1 = VehicleImpl.Builder.newInstance("2_1").setStartLocationCoordinate(Coordinate.newInstance(40, 40)).setType(type2).build();
		vrpBuilder.addVehicle(vehicle2_1);
		//1xtype3
		VehicleType type3 = VehicleTypeImpl.Builder.newInstance("type_3").addCapacityDimension(0, 300).setCostPerDistance(1.3).build();
		VehicleImpl vehicle3_1 = VehicleImpl.Builder.newInstance("3_1").setStartLocationCoordinate(Coordinate.newInstance(40, 40)).setType(type3).build();
		vrpBuilder.addVehicle(vehicle3_1);
		
		//add penaltyVehicles to allow invalid solutions temporarily
		vrpBuilder.addPenaltyVehicles(5, 1000);
		
		//set fleetsize finite
		vrpBuilder.setFleetSize(FleetSize.FINITE);
		
		//build problem
		VehicleRoutingProblem vrp = vrpBuilder.build();
		
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "input/algorithmConfigWithSchrimpfAcceptance.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);
		
		SolutionPrinter.print(vrp, best, Print.VERBOSE);
		
		new GraphStreamViewer(vrp, best).setRenderDelay(100).display();
		
	}

}
