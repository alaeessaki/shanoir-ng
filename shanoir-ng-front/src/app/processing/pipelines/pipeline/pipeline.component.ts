import { Component, Input, OnInit } from '@angular/core';
import { Pipeline } from 'src/app/carmin/models/pipeline';

@Component({
  selector: 'app-pipeline',
  templateUrl: './pipeline.component.html',
  styleUrls: ['./pipeline.component.css']
})
export class PipelineComponent implements OnInit {

  @Input() pipeline:Pipeline;

  constructor() { }

  ngOnInit(): void {
    console.log(this.pipeline);
  }

}
