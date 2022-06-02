/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2022 Inria - https://www.inria.fr/
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

import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { CarminDatasetProcessing } from "../models/CarminDatasetProcessing";
import * as AppUtils from "../../utils/app.utils";
import { Observable } from "rxjs";


@Injectable()
export class CarminDatasetProcessingService {

    basePath: string = AppUtils.BACKEND_API_CARMIN_DATASET_PROCESSING_URL;

    constructor(protected httpClient: HttpClient) { }


    public saveNewCarminDatasetProcessing(carminDatasetProcessing: CarminDatasetProcessing): Observable<Object> {
        return this.httpClient.post<Object>(this.basePath, carminDatasetProcessing);
    }


}
