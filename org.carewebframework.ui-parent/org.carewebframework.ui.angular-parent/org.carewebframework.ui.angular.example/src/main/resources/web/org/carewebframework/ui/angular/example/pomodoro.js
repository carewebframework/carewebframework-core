"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
var core_1 = require("@angular/core");
// Pomodoro timer component
var AngularComponent = (function () {
    function AngularComponent() {
        var _this = this;
        this.zk = System.paths['zk:'];
        this.resetPomodoro();
        setInterval(function () { return _this.tick(); }, 1000);
    }
    AngularComponent.prototype.resetPomodoro = function () {
        this.isPaused = true;
        this.minutes = 24;
        this.seconds = 59;
        this.buttonLabel = 'Start';
    };
    AngularComponent.prototype.tick = function () {
        if (!this.isPaused) {
            this.buttonLabel = 'Pause';
            if (--this.seconds < 0) {
                this.seconds = 59;
                if (--this.minutes < 0) {
                    this.resetPomodoro();
                }
            }
        }
    };
    AngularComponent.prototype.togglePause = function () {
        this.isPaused = !this.isPaused;
        if (this.minutes < 24 || this.seconds < 59) {
            this.buttonLabel = this.isPaused ? 'Resume' : 'Pause';
        }
    };
    return AngularComponent;
}());
AngularComponent = __decorate([
    core_1.Component({
        template: "\n    <div class=\"text-center\">\n      <img src=\"{{zk}}org/carewebframework/ui/angular/example/assets/img/pomodoro.png\" alt=\"Pomodoro\">\n      <h1> {{ minutes }}:{{ seconds | number: '2.0' }} </h1>\n      <p>\n        <button (click)=\"togglePause()\"\n          class=\"btn btn-danger\">\n          {{ buttonLabel }}\n        </button>\n      </p>\n    </div>\n"
    }),
    __metadata("design:paramtypes", [])
], AngularComponent);
exports.AngularComponent = AngularComponent;
