package neoImage.v2;

public class ResultUnitPair<A, B> {
    private A value;
    private B units;

    public ResultUnitPair(A value, B units) {
        super();
        this.value = value;
        this.units = units;
    }

    public int hashCode() {
        int hashvalue = value != null ? value.hashCode() : 0;
        int hashunits = units != null ? units.hashCode() : 0;

        return (hashvalue + hashunits) * hashunits + hashvalue;
    }

    public boolean equals(Object other) {
        if (other instanceof ResultUnitPair) {
        	ResultUnitPair<?, ?> otherPair = (ResultUnitPair<?, ?>) other;
            return 
            ((  this.value == otherPair.value ||
                ( this.value != null && otherPair.value != null &&
                  this.value.equals(otherPair.value))) &&
             (  this.units == otherPair.units ||
                ( this.units != null && otherPair.units != null &&
                  this.units.equals(otherPair.units))) );
        }

        return false;
    }

    public String toString()
    { 
           return value + " " + units; 
    }
    
    public boolean isNull() {
        if(this.value == null && this.units == null) {
        	return true;
        }else{
        	return false;
        }
    }

    public A getValue() {
        return value;
    }
    
    public String getValueAsString() {
    	if(this.value==null) {
    		return null;
    	}else {
    		return String.valueOf(value);
    	}
    }

    public void setValue(A value) {
        this.value = value;
    }

    public B getUnits() {
        return units;
    }

    public void setUnits(B units) {
        this.units = units;
    }
    
    public ResultUnitPair<A, B> clear() {
        this.value = null;
        this.units = null;
        return(this);
    }
}
