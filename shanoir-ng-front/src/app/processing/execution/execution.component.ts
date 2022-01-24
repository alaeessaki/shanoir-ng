import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { ParameterType } from 'src/app/carmin/models/parameterType';
import { Pipeline } from 'src/app/carmin/models/pipeline';
import { CarminClientService } from 'src/app/carmin/shared/carmin-client.service';
import { ProcessingService } from '../processing.service';

@Component({
  selector: 'app-execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

  pipeline: Pipeline;
  
  constructor(private breadcrumbsService: BreadcrumbsService, private processingService: ProcessingService, private carminClientService:CarminClientService, private router:Router) { 
    this.breadcrumbsService.nameStep('2. Execution');
  }

  ngOnInit(): void {
    if(!this.processingService.isAnyPipelineSelected()){
      this.router.navigate(["/processing/pipelines"])
    }

    this.processingService.selectedPipeline.subscribe(
      (pipeline: Pipeline)=>{
        this.pipeline = pipeline;
        console.log(pipeline);
      }
    )
  }

  getParameterType(parameterType: ParameterType){
    switch(parameterType){
      case ParameterType.String:
      case ParameterType.Boolean: return 'text';
      case ParameterType.Int64:
      case ParameterType.Double: return 'number';
      case ParameterType.File: return 'file';
    }
  }

}
