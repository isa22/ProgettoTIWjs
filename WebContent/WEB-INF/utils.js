/**
 * AJAX call management
 */

	//This function is used to send a request that contains a FormData obj 
	function makeFormCall(method, url, formData, cback, reset = true) {
	    var req = new XMLHttpRequest(); // visible by closure
	    req.onreadystatechange = function() {
	      cback(req)
	    }; // closure (with this closure the callback function is called when the response is received)
	       // since it is a closure, req will be visible to the callback function even if the current 
	       // function has already terminated its execution.
	    req.open(method, url);
	    if (formData == null) {
	      req.send();
	    } else {
	      req.send(formData);
	    }
	    if (formData !== null && reset === true) {
	      formElement.reset();
	    }
	  }
	
	//This function is used to send a request that contains a URLSearchParams obj
	function makeSearchCall(method, url, urlSearchParams, cback, reset = true) {
	    var req = new XMLHttpRequest(); // visible by closure
	    req.onreadystatechange = function() {
	      cback(req)
	    }; // closure
	    req.open(method, url);
	    if (formData == null) {
	      req.send();
	    } else {
	      req.send(urlSearchParams);
	    }
	    if (formData !== null && reset === true) {
	      formElement.reset();
	    }
	  }
