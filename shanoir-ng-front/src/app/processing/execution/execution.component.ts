import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { BreadcrumbsService } from 'src/app/breadcrumbs/breadcrumbs.service';
import { Execution } from 'src/app/carmin/models/execution';
import { ParameterType } from 'src/app/carmin/models/parameterType';
import { Pipeline } from 'src/app/carmin/models/pipeline';
import { CarminClientService } from 'src/app/carmin/shared/carmin-client.service';
import { MsgBoxService } from 'src/app/shared/msg-box/msg-box.service';
import { ProcessingService } from '../processing.service';
import { KeycloakService } from 'src/app/shared/keycloak/keycloak.service';



@Component({
  selector: 'app-execution',
  templateUrl: './execution.component.html',
  styleUrls: ['./execution.component.css']
})
export class ExecutionComponent implements OnInit {

  pipeline: Pipeline;
  executionForm: FormGroup;
  selectedDatasets: Set<number>;
  token:String;

  constructor(private breadcrumbsService: BreadcrumbsService, private processingService: ProcessingService, private carminClientService:CarminClientService, private router:Router,private msgService: MsgBoxService, private keycloakService: KeycloakService) { 
    this.breadcrumbsService.nameStep('2. Execution');
    this.selectedDatasets = new Set();
  }

  ngOnInit(): void {
    if(!this.processingService.isAnyPipelineSelected()){
      this.router.navigate(["/processing/pipelines"])
    }

    this.processingService.selectedPipeline.subscribe(
      (pipeline: Pipeline)=>{
        this.pipeline = pipeline;
        this.initExecutionForm()
      }
    )
    this.processingService.selectedDatasets.subscribe(
      (datasets: Set<number>)=>{
        this.selectedDatasets = datasets;
      }
    )
    
    this.keycloakService.getToken().then(
      (token)=>{
        this.token = token;
      }
    ).catch(
      (err)=>{
        console.error(err)
      }
    );
  }

  initExecutionForm(){
    this.executionForm = new FormGroup({
      "execution_name": new FormControl('', Validators.required)
    });

    this.pipeline.parameters.filter(p => p.type != ParameterType.File).forEach(
      parameter=>{
        let control = new FormControl(parameter.defaultValue);
        let validators:Validators[] = [];
        if(!parameter.isOptional) validators.push(Validators.required);
        this.executionForm.addControl(parameter.name, control);
      }
    )

     console.log(this.executionForm);
  }

  onSubmitExecutionForm(){
    let execution:Execution = new Execution();
    execution.name = this.executionForm.get("execution_name").value;
    execution.pipelineIdentifier = this.pipeline.identifier;
    execution.timeout = 20;
    execution.inputValues = {};
    this.pipeline.parameters.forEach(
      parameter=>{
        console.log(this.executionForm);
        console.log(this.executionForm.get(parameter.name))
        if(parameter.type == ParameterType.File){
          // TODO this  platform identifier ("shanoir") should be in a constant file
          // TODO the file order should be specified automaticaly, with the help of the UI or the order.
           execution.inputValues[parameter.name]= "shanoir:/download"+[...this.selectedDatasets][0]+".dcm?format=dcm&datasetId="+[...this.selectedDatasets][0]+"&token="+this.token+"&refreshToken=qsjdqmskldmsqd&outName="+this.executionForm.get("out_name").value+".tgz&md5=none&type=File";
           if(parameter.name=="executable"){
	      execution.inputValues[parameter.name]= "file:/var/www/html/workflows/SharedData/groups/Support/Applications/testGME2inputFiles/1.0/bin/testGME2inputFiles.sh.tar.gz";
 	   }
	 }else{
          execution.inputValues[parameter.name]=this.executionForm.get(parameter.name).value;
        }
      }
    )
    /*
     * Init result location
     */
    execution.resultsLocation = "shanoir:/download"+[...this.selectedDatasets][0]+".dcm?format=dcm&datasetId="+[...this.selectedDatasets][0]+"&token="+this.token+"&refreshToken=qsjdqmskldmsqd&outName="+this.executionForm.get("out_name").value+".tgz&md5=none&type=File";
    console.log(execution);
    this.carminClientService.createExecution(execution).subscribe(
      (_)=>{
          console.log("executed !");
          this.msgService.log('info', 'the execution successfully started.')
      },
      (error)=>{
          this.msgService.log('error', 'Sorry, an error occurred while starting the execution.');
      }
  )
  }

  getParameterType(parameterType: ParameterType): String{
    switch(parameterType){
      case ParameterType.String:
      case ParameterType.Boolean: return 'text';
      case ParameterType.Int64:
      case ParameterType.Double: return 'number';
      case ParameterType.File: return 'file';
    }
  }

  isAFile(parameterType: ParameterType): boolean{
    if(parameterType ==  ParameterType.File) return true;
    return false;
  }

}
