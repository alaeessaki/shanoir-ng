import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ProcessingService {

  private selectedDatasetsSubject: BehaviorSubject<Set<number>>;
  public selectedDatasets: Observable<Set<number>>;

  constructor() { 
    this.selectedDatasetsSubject = new BehaviorSubject<Set<number>>(new Set());
    this.selectedDatasets = this.selectedDatasetsSubject.asObservable();
  }

  public get selectedDatasetsValue(): Set<number>{
    return this.selectedDatasetsSubject.value;
  }

  public clearDatasets(): void{
    this.selectedDatasetsSubject.next(new Set());
  }

  public setDatasets(datasetsIds : Set<number>){
    this.selectedDatasetsSubject.next(datasetsIds);
  }

  public isDatasetsSubjectValid():boolean{
    let selectedDatasets: Set<number>;
    this.selectedDatasets.subscribe(
      (datasets: Set<number>)=>{
        selectedDatasets = datasets;
      }
    );

    if(selectedDatasets == null || selectedDatasets.size == 0){
      return false;
    }
    return true;
  }
}
