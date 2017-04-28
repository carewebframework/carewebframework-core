System.config(
        {	
        	'paths': {
        		'zk:': zk.ajaxURI('web/', {au: true})
        	},
        	
        	'map': {
        		'cwf-angular-bootstrap': 'zk:angular/cwf/bootstrap.js',
		      	'@angular/core': 'zk:angular/@angular/core/bundles/core.umd.js',
		      	'@angular/common': 'zk:angular/@angular/common/bundles/common.umd.js',
		      	'@angular/compiler': 'zk:angular/@angular/compiler/bundles/compiler.umd.js',
		      	'@angular/platform-browser': 'zk:angular/@angular/platform-browser/bundles/platform-browser.umd.js',
		      	'@angular/platform-browser-dynamic': 'zk:angular/@angular/platform-browser-dynamic/bundles/platform-browser-dynamic.umd.js',
		      	'@angular/http': 'zk:angular/@angular/http/bundles/http.umd.js',
		      	'@angular/router': 'zk:angular/@angular/router/bundles/router.umd.js',
		      	'@angular/forms': 'zk:angular/@angular/forms/bundles/forms.umd.js',
        		'rxjs': 'zk:angular/rxjs',
        		'core-js': 'zk:angular/core-js',
        		'zone.js': 'zk:angular/zone.js/dist/zone.js'
        	},
        	
        	'packages': {
        		'rxjs': {
        			'defaultExtension': 'js'
        		},
        		'core-js': {
        			'defaultExtension': 'js'
        		}
        	}
		}
);
