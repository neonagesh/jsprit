package jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.job.Shipment;

import org.junit.Before;
import org.junit.Test;

public class DeliverShipmentTest {
	
	private Shipment shipment;
	
	private DeliverShipment deliver;
	
	@Before
	public void doBefore(){
		shipment = Shipment.Builder.newInstance("shipment").setPickupLocation("pickupLoc")
				.setDeliveryLocation("deliveryLoc")
				.setPickupTimeWindow(TimeWindow.newInstance(1., 2.))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3., 4.))
				.addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
		deliver = new DeliverShipment(shipment);
	}
	
	@Test
	public void whenCallingCapacity_itShouldReturnCorrectCapacity(){
		assertEquals(-10,deliver.getSize().get(0));
		assertEquals(-100,deliver.getSize().get(1));
		assertEquals(-1000,deliver.getSize().get(2));
	}

	@Test
	public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly(){
		assertEquals(3.,deliver.getTheoreticalEarliestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly(){
		assertEquals(4.,deliver.getTheoreticalLatestOperationStartTime(),0.01);
	}
	
	@Test
	public void whenSettingArrTime_itShouldBeSetCorrectly(){
		deliver.setArrTime(4.0);
		assertEquals(4.,deliver.getArrTime(),0.01);
	}
	
	@Test
	public void whenSettingEndTime_itShouldBeSetCorrectly(){
		deliver.setEndTime(5.0);
		assertEquals(5.,deliver.getEndTime(),0.01);
	}
	
	@Test
	public void whenIniLocationId_itShouldBeSetCorrectly(){
		assertEquals("deliveryLoc",deliver.getLocationId());
	}
	
	@Test
	public void whenCopyingStart_itShouldBeDoneCorrectly(){
		DeliverShipment copy = (DeliverShipment) deliver.duplicate();
		assertEquals(3.,copy.getTheoreticalEarliestOperationStartTime(),0.01);
		assertEquals(4.,copy.getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals("deliveryLoc",copy.getLocationId());
		assertEquals(-10,copy.getSize().get(0));
		assertEquals(-100,copy.getSize().get(1));
		assertEquals(-1000,copy.getSize().get(2));
		assertTrue(copy!=deliver);
	}

	
	@Test
	public void whenGettingCapacity_itShouldReturnItCorrectly(){
		Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation("pickLoc").setDeliveryLocation("delLoc")
				.addSizeDimension(0, 10).addSizeDimension(1, 100).build();
		PickupShipment pick = new PickupShipment(shipment);
		assertEquals(10,pick.getSize().get(0));
		assertEquals(100,pick.getSize().get(1));
	}

}
