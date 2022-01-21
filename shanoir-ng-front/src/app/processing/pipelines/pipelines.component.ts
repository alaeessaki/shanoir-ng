import { Component, OnInit } from '@angular/core';
import { Pipeline } from 'src/app/carmin/models/pipeline';
import { CarminClientService } from 'src/app/carmin/shared/carmin-client.service';

@Component({
  selector: 'app-pipelines',
  templateUrl: './pipelines.component.html',
  styleUrls: ['./pipelines.component.css']
})
export class PipelinesComponent implements OnInit {

  pipelines:Pipeline[];
  selectedPipeline:Pipeline;
  descriptionLoading:boolean;

  constructor(private carminClientService: CarminClientService) { 
    this.pipelines = [];
    this.descriptionLoading = false;
  }

  ngOnInit(): void {
    this.carminClientService.listPipelines().subscribe(
      (pipelines :Pipeline[])=>{
        this.pipelines = pipelines;
      }
    )
  }

  selectPipeline(pipeline:Pipeline){
    this.descriptionLoading = true;
    this.carminClientService.getPipeline(pipeline.identifier).subscribe(
      (pipeline:Pipeline)=>{
        this.descriptionLoading = false;
        this.selectedPipeline = pipeline;
        console.log(pipeline);
      },
      (error)=>{
        console.error(error);
      }
    )
  }

}
