package net.sf.openrocket.rocketcomponent;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.util.MathUtil;

/** 
 * WARNING:  This class is only partially implemented.  Recomend a bit of testing before you attach it to the GUI.
 * @author widget (Daniel Williams)
 *
 */
public class RailButton extends ExternalComponent implements LineInstanceable {
	
	private static final Translator trans = Application.getTranslator();
	
	// NOTE: Rail Button ARE NOT STANDARD -- They vary by manufacturer, and model.
	// These presets have appropriate dimensions for each rail size, given the Rail Buttons contribute so little to flying properties. 
	public static final RailButton ROUND_1010 = make1010Button();
	public static final RailButton ROUND_1515 = make1515Button();
	
	/*
	 * Rail Button Dimensions (side view)
	 * 
	 *    <= outer dia  =>
	 *                                   v
	 *   ^     [[[[[[]]]]]]              flangeHeight
	 * total    >||||||<=  inner dia     ^
	 * height    ||||||       v
	 *   v     [[[[[[]]]]]]  standoff == baseHeight 
	 *     ================   ^
	 *          (body)
	 *   
	 */
	protected double outerDiameter_m;
	protected double height_m;
	protected double innerDiameter_m;
	protected double flangeHeight_m;
	
	// Standoff is defined as the distance from the body surface to this components reference point
	// Note:  the reference point for Rail Button Components is in the center bottom of the button. 
 	protected double standoff;
	
	protected final static double MINIMUM_STANDOFF= 0.001;
	
	private double radialDistance_m=0;
	private double angle_rad = 0;
	private int instanceCount = 1;
	private double instanceSeparation = 0; // front-front along the positive rocket axis. i.e. [1,0,0];
	
	public RailButton(){
		super(Position.MIDDLE);
		this.outerDiameter_m = 0;
		this.height_m = 0;		
		this.innerDiameter_m = 0;
		this.flangeHeight_m = 0;
		this.setStandoff( 0);
		this.setInstanceSeparation( 1.0);
	}
	
	public RailButton( final double od, final double ht ) {
		this();
		this.setOuterDiameter( od);
		this.setTotalHeight( ht);
	}
	
	public RailButton( final double od, final double id, final double h, final double _thickness, final double _standoff ) {
		super(Position.MIDDLE);
		this.outerDiameter_m = od;
		this.height_m = h-_standoff;		
		this.innerDiameter_m = id;
		this.flangeHeight_m = _thickness;
		this.setStandoff( _standoff);
		this.setInstanceSeparation( od*2);
	}
	
	private static final RailButton make1010Button(){
		final double id = 0.008; // guess
		final double od = 0.0097;
		final double ht = 0.0097;
		final double thickness = 0.002; // guess
		final double standoff = 0.002; // guess
		RailButton rb1010 = new RailButton( od, id, ht, thickness, standoff);
		rb1010.setMassOverridden(true);
		rb1010.setOverrideMass(0.0019);
		
		rb1010.setInstanceCount(1);
		rb1010.setInstanceSeparation( od*6 );
		return rb1010;
	}
	
	private static final RailButton make1515Button(){
		final double id = 0.012; // guess
		final double od = 0.016;
		final double ht = 0.0173;
		final double thickness = 0.0032; // guess
		final double standoff = 0.0032;  // guess
		RailButton rb1010 = new RailButton( od, id, ht, thickness, standoff);
		rb1010.setMassOverridden(true);
		rb1010.setOverrideMass(0.0077);
		
		return rb1010;
	}
	
	public double getStandoff(){
		return this.standoff;
	}

	public double getOuterDiameter() {
		return this.outerDiameter_m;
	}
	
	public double getInnerDiameter() {
		return this.innerDiameter_m;
	}
	
	public double getInnerHeight() {
		return (this.height_m - this.flangeHeight_m);
	}
	
	public double getTotalHeight() {
		return this.height_m+this.standoff;
	}
	
	public double getFlangeHeight() {
		return this.flangeHeight_m;
	}
	
	public double getBaseHeight(){
		return this.getStandoff();
	}
	
	public void setStandoff( final double newStandoff){
		this.standoff = Math.max( newStandoff, RailButton.MINIMUM_STANDOFF );
	}

	public void setInnerDiameter( final double newID ){
		this.innerDiameter_m = newID;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}


	public void setOuterDiameter( final double newOD ){
		this.outerDiameter_m = newOD;
		if( 0 == this.innerDiameter_m){
			this.innerDiameter_m = this.outerDiameter_m*0.8;
		}
		if( 0 == this.instanceSeparation ){
			this.instanceSeparation = newOD*8;
		}
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}

	public void setTotalHeight( final double newHeight ) {
		if( 0 == this.flangeHeight_m){
			this.flangeHeight_m = newHeight*0.25;
		}
		if( 0 == this.standoff){
			this.height_m = newHeight*0.75;
			this.offset = newHeight*0.25;
		}else{
			this.height_m = newHeight-this.standoff;
		}
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	public void setThickness( final double newThickness ) {
		this.flangeHeight_m = newThickness;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	@Override
	public boolean isAerodynamic(){
		// TODO: implement aerodynamics
		return false;
	}
	
	public double getAngularOffset(){
		return angle_rad;
	}
	
	public void setAngularOffset(final double angle_rad){
		double clamped_rad = MathUtil.clamp(angle_rad, -Math.PI, Math.PI);
		
		if (MathUtil.equals(this.angle_rad, clamped_rad))
			return;
		this.angle_rad = clamped_rad;
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
	
	@Override
	public void setRelativePosition(RocketComponent.Position position) {
		super.setRelativePosition(position);
		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
	}
	
//	
//	@Override
//	public void setPositionValue(double value) {
//		super.setPositionValue(value);
//		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
//	}


	@Override
	public Coordinate[] getInstanceOffsets(){
		Coordinate[] toReturn = new Coordinate[this.getInstanceCount()];
		
		final double xOffset = this.position.x;
		final double yOffset = Math.cos(this.angle_rad) * ( this.radialDistance_m );
		final double zOffset = Math.sin(this.angle_rad) * ( this.radialDistance_m );
		
		for ( int index=0; index < this.getInstanceCount(); index++){
			toReturn[index] = new Coordinate(xOffset + index*this.instanceSeparation, yOffset, zOffset);
		}
		
		return toReturn;
	}
	
//	@Override
//	protected void loadFromPreset(ComponentPreset preset) {
//		if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
//			double outerDiameter = preset.get(ComponentPreset.OUTER_DIAMETER);
//			this.radius = outerDiameter / 2.0;
//			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
//				double innerDiameter = preset.get(ComponentPreset.INNER_DIAMETER);
//				this.thickness = (outerDiameter - innerDiameter) / 2.0;
//			}
//		}
//		
//		super.loadFromPreset(preset);
//		
//		fireComponentChangeEvent(ComponentChangeEvent.BOTH_CHANGE);
//	}
//	
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.LAUNCH_LUG;
	}
	
	
//	@Override
//	protected Coordinate[] shiftCoordinates(Coordinate[] array) {
//		array = super.shiftCoordinates(array);
//		
//		for (int i = 0; i < array.length; i++) {
//			array[i] = array[i].add(0, shiftY, shiftZ);
//		}
//		
//		return array;
//	}
	
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		super.componentChanged(e);
		
		RocketComponent body;
		double parentRadius=0;
		
		for (body = this.getParent(); body != null; body = body.getParent()) {
			if (body instanceof BodyTube)
				parentRadius = ((BodyTube) body).getOuterRadius();
		}
		
		this.radialDistance_m = parentRadius;
	}
	
	
	@Override
	public double getComponentVolume() {
		final double volOuter = Math.PI*Math.pow( outerDiameter_m/2, 2)*flangeHeight_m;
		final double volInner = Math.PI*Math.pow( innerDiameter_m/2, 2)*(height_m - flangeHeight_m - standoff);
		final double volStandoff = Math.PI*Math.pow( outerDiameter_m/2, 2)*standoff;
		return (volOuter+
				volInner+
				volStandoff);
	}


	
	@Override
	public double getInstanceSeparation(){
		return this.instanceSeparation;
	}
	
	@Override
	public void setInstanceSeparation(final double _separation){
		this.instanceSeparation = _separation;
	}
	
	@Override
	public void setInstanceCount( final int newCount ){
		if( 0 < newCount ){
			this.instanceCount = newCount;
		}
	}
	
	@Override
	public int getInstanceCount(){
		return this.instanceCount;
	}

	@Override
	public String getPatternName(){
		return (this.getInstanceCount() + "-Line");
	}

	@Override
	public Collection<Coordinate> getComponentBounds() {
		ArrayList<Coordinate> set = new ArrayList<Coordinate>();
		return set;
	}
	
	@Override
	public Coordinate getComponentCG() {
		// Math.PI and density are assumed constant through calcualtion, and thus may be factored out. 
		final double volumeInner = Math.pow( innerDiameter_m/2, 2)*(height_m - flangeHeight_m - standoff);
		final double volumeOuter = Math.pow( outerDiameter_m/2, 2)*flangeHeight_m;
		final double volumeStandoff = Math.pow( outerDiameter_m/2, 2)*standoff;
		final double totalVolume = volumeInner + volumeOuter + volumeStandoff;
		final double heightCM = (volumeInner*( this.height_m - this.flangeHeight_m/2) + volumeOuter*( this.height_m-this.flangeHeight_m)/2 - volumeStandoff*(this.standoff/2))/totalVolume;

		if( heightCM > this.height_m ){
			throw new BugException(" bug found while computing the CG of a RailButton: "+this.getName()+"\n height of CG: "+heightCM);
		}
		
		final double CMy = Math.cos(this.angle_rad)*heightCM;
		final double CMz = Math.sin(this.angle_rad)*heightCM;
		
		return new Coordinate( 0, CMy, CMz, getComponentMass());
	}
	
	@Override
	public String getComponentName() {
		// Launch Button
		return trans.get("LaunchButton.LaunchButton");
	}
	
	@Override
	public double getLongitudinalUnitInertia() {
		// 1/12 * (3 * (r2^2 + r1^2) + h^2)
//		return (3 * (MathUtil.pow2(getOuterRadius()) + MathUtil.pow2(getInnerRadius())) + MathUtil.pow2(getLength())) / 12;
		return 0.0;
	}
	
	@Override
	public double getRotationalUnitInertia() {
		// 1/2 * (r1^2 + r2^2)
//		return (MathUtil.pow2(getInnerRadius()) + MathUtil.pow2(getOuterRadius())) / 2;
		return 0.0;
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		// Allow nothing to be attached to a LaunchButton
		return false;
	}
	
}
