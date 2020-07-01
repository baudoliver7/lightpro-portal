(function(app){
	'use strict';

	app.controller('purchasesCtrl', purchasesCtrl);

	purchasesCtrl.$inject = ['$scope', 'apiService'];
	function purchasesCtrl($scope, apiService) {
	    var vm = this;
	    var features = [];
	    
	    $scope.isGranted = function(access) {
			
			var granted = false;

		    angular.forEach(features, function (value) {
		        if (value.id == access)
	        		granted = true;
		    });

		    return granted;
		};
		
	    $(document).ready(function() {
	    	  $('[data-toggle=offcanvas]').click(function() {
	    	    $('.row-offcanvas').toggleClass('active');
	    	  });	    	  	    	  
	    	});
	    
	    this.$onInit = function(){
			apiService.get(String.format("/web/api/membership/user/current/module/{0}/feature", 9), {},
					function(response){
						features = response.data;
					}
			);
		}
	}
})(angular.module('lightpro'));