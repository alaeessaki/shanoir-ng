/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.dataset.modality;

import javax.persistence.Entity;

import org.shanoir.ng.dataset.model.Dataset;

/**
 * CT dataset.
 * 
 * @author msimon
 *
 */
@Entity
public class CtDataset extends Dataset {

	public static final String datasetType = "Ct";

//	private String pixelData;
//	
//	private String patientExamination;
//	
//	private String imageFlavor;
//	
//	private String derivedPixelContrast;
	
	/**
	 * UID
	 */
	private static final long serialVersionUID = -1035190618348031062L;

	@Override
	public String getType() {
		return "Ct";
	}

}
