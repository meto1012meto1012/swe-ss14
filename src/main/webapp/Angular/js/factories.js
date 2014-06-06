Shop.factory('ArtikelService', function($resource){
	var ArtikelService = $resource('/shop/rest/artikel/:artikelid', {}, {});
	
	ArtikelService.findAllArtikel = function(id) {
		return ArtikelService.get({artikelid : id});
	};
	
	
	
	return ArtikelService;
});

Shop.factory('ArtikelFactory', function ($resource) {
    return $resource('/shop/rest/artikel/', {}, {
        query: { method: 'GET', isArray: false },
        create: { method: 'POST' }
	 });
    });


Shop.factory('ArtikelFactory', function ($resource) {
    return $resource('/shop/rest/artikel/:id', {}, {
        show: { method: 'GET' },
        'update': { method: 'PUT' }
    });
});

	


