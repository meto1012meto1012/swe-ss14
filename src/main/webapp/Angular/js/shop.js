var Shop = angular.module('shop', ['ngResource']);

		Shop.config(function ($routeProvider) {
        $routeProvider.
            when('/artikelA', {templateUrl: 'partials/alleArtikel.html', controller: 'alleArtikelController'}).
            when('/artikelS', {templateUrl: 'partials/artikelAnlegen.html', controller: 'artikelAnlegenController'}).
            when('/artikelU', {templateUrl: 'partials/artikelAendern.html', controller: 'artikelAendernController'}).
			when('/kunde', {templateUrl: 'partials/alleKunden.html', controller: 'alleKundenController'}).
			when('/', {templateUrl: 'partials/home.html'});
    });


Shop.controller('alleArtikelController', ['$scope', 'ArtikelService', '$location',
	function($scope, ArtikelService, $location){
	
		$scope.search = function(aid){
			$scope.artikel = ArtikelService.findAllArtikel(aid);
		};

	$scope.editArtikel = function (aid) {
            $location.path('/artikelU/').search('id', aid);
        };
}]);


Shop.controller('artikelAendernController', ['$scope', '$routeParams', 'ArtikelFactory', '$location',
    function ($scope, $routeParams, ArtikelFactory, $location) {

		
	  $scope.updateArtikel = function () {
			
            var artikel = ArtikelFactory.get({id: $routeParams.id});
            $id = artikel.id;
			artikel.bezeichnung = $scope.bezeichnung; 
			artikel.preis = $scope.preis; 
			
			ArtikelFactory.update({id:$id}, artikel);
			$location.path('/artikelA');
        };
      
        $scope.cancel = function () {
            $location.path('/home');
        };

	
        $scope.artikel = ArtikelFactory.show({id: $routeParams.id});
    }]);


Shop.controller('alleKundenController', ['$scope', 'KundenService', 
	function($scope, KundenService){
	

	$scope.search = function(kid){
	$scope.kunden = KundenService.findAllKunden(kid);
	};

}]);

Shop.controller('artikelAnlegenController', ['$scope', 'ArtikelFac', '$location',
    function ($scope, ArtikelFac, $location) {
	
        $scope.createArtikel = function () {
            ArtikelFac.create($scope.artikel);
			$location.path('/artikelA');
			alert(this.id);
        };
		            
		
    }]);