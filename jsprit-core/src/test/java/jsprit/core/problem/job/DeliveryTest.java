package jsprit.core.problem.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DeliveryTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenNeitherLocationIdNorCoordIsSet_itThrowsException(){
		Delivery.Builder.newInstance("p").build();
	}
	
	@Test
	public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo(){
		Delivery one = (Delivery)Delivery.Builder.newInstance("s").setLocationId("foofoo")
				.addSizeDimension(0,2)
				.addSizeDimension(1,4)
				.build();
		assertEquals(2,one.getSize().getNuOfDimensions());
		assertEquals(2,one.getSize().get(0));
		assertEquals(4,one.getSize().get(1));
		
	}
	
	@Test
	public void whenPickupIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero(){
		Delivery one = (Delivery)Delivery.Builder.newInstance("s").setLocationId("foofoo")
				.build();
		assertEquals(1,one.getSize().getNuOfDimensions());
		assertEquals(0,one.getSize().get(0));
	}
	
	@Test
	public void whenPickupIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly(){
		Delivery one = (Delivery)Delivery.Builder.newInstance("s").addSizeDimension(0, 1).setLocationId("foofoo")
				.build();
		assertEquals(1,one.getSize().getNuOfDimensions());
		assertEquals(1,one.getSize().get(0));
	}


}
