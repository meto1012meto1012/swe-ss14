Shop.factory('ArtikelService', function($resource){
	var ArtikelService = $resource('/shop/rest/artikel/:artikelid', {}, {});
	
	ArtikelService.findAllArtikel = function(id) {
		return ArtikelService.get({artikelid : id});
	};
	
	return ArtikelService;
});