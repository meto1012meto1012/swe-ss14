Shop.factory('ArtikelService', function($resource){
	var ArtikelService = $resource('/shop/rest/artikel/:artikelid', {}, {});
	
	ArtikelService.findAllArtikel = function(id) {
		return ArtikelService.get({artikelid : id});
	};
	
	
	
	return ArtikelService;
});

Shop.factory('ArtikelFactory', ['$resource', function ($resource) {
    return $resource('/shop/rest/artikel/:id', {}, {
        show: { method: 'GET' },
        'update': { method: 'PUT'}
    });
}]);

Shop.factory('KundenService', function($resource){
	var KundenService = $resource('/shop/rest/kunden/:kundenid', {}, {});
	
	KundenService.findAllKunden = function(id) {
			return KundenService.get({kundenid : id});

	};
	
	
	
	return KundenService;
});

Shop.factory('ArtikelFac', function ($resource) {
    return $resource('/shop/rest/artikel', {}, {
        query: { method: 'GET', isArray: true },
        create: { method: 'POST' }
	 });
    });

