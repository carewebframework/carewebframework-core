import { Component } from '@angular/core';

var i = 0;

// Our first "Hello world" component
@Component({
   template: '<h1 class="text-center"> {{greeting}} </h1>'
})
export class AngularComponent {
  greeting: string;
  constructor() {
    this.greeting = 'Hello Angular 2 instance #' + ++i;
  }
}


