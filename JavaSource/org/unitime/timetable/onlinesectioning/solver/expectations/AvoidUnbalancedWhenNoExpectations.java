/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.solver.expectations;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

/**
 * @author Tomas Muller
 */
public class AvoidUnbalancedWhenNoExpectations extends PercentageOverExpected {
	private Double iDisbalance = 0.1;

	public AvoidUnbalancedWhenNoExpectations(DataProperties config) {
		super(config);
		iDisbalance = config.getPropertyDouble("OverExpected.Disbalance", iDisbalance);
	}
	
	public AvoidUnbalancedWhenNoExpectations(Double percentage, Double disbalance) {
		super(percentage);
		iDisbalance = disbalance;
	}
	
	public AvoidUnbalancedWhenNoExpectations(Double percentage) {
		this(percentage, null);
	}
	
	public AvoidUnbalancedWhenNoExpectations() {
		this(null, null);
	}
	
	public Double getDisbalance() {
		return iDisbalance;
	}
	
	@Override
	public double getOverExpected(Section section, Request request) {
		Subpart subpart = section.getSubpart();

		if (hasExpectations(subpart) && section.getLimit() > 0)
			return super.getOverExpected(section, request);
		
		if (getDisbalance() == null || getDisbalance() < 0.0) return 0.0;
		
		double enrlConfig = request.getWeight() + getEnrollment(subpart.getConfig(), request);
		int subparts = section.getSubpart().getConfig().getSubparts().size();
		int limit = getLimit(section);
    	double enrl = request.getWeight() + getEnrollment(section, request);

		if (limit > 0) {
            // sections have limits -> desired size is section limit x (total enrollment / total limit)
            double desired = (enrlConfig / getLimit(subpart)) * limit;
            if (enrl - desired >= Math.max(1.0, getDisbalance() * limit))
            	return 1.0 / subparts;
        } else {
            // unlimited sections -> desired size is total enrollment / number of sections
        	double desired = enrlConfig / subpart.getSections().size();
        	if (enrl - desired >= Math.max(1.0, getDisbalance() * desired))
        		return 1.0 / subparts;
        }
		
		return 0.0;
	}
	
	@Override
	public String toString() {
		return "bal(" + getPercentage() + "," + getDisbalance() + ")";
	}
	
}