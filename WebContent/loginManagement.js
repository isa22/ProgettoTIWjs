/**
 * Login management
 */

(function() { // avoid variables ending up in the global scope

  //function used to validate email using a regular expression (https://stackoverflow.com/questions/46155/how-to-validate-an-email-address-in-javascript)
  let validateEmail = function (email){
	    const re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
	    return re.test(String(email).toLowerCase());
  };
	
  //login submitted event
  document.getElementById("loginButton").addEventListener('click', (e) => {
	  
	  console.log("Loggign in...");
    
	  //getting forms reference
	  var username = document.getElementById("loginUsername");
	  var password = document.getElementById("loginPassword");
	      
	  //form content validity is checked
	  if (username.checkValidity() && password.checkValidity()) { 
    	
		  //retrieving data from forms and putting them in formData
		  var formData = new FormData();
		  formData.append("username",username.value);
		  console.log("User: " + username.value);
		  formData.append("password",password.value);
          
		  //sending form to server
		  makeFormCall("POST", 'Login', formData,
          
		  //callback for response
		  function(req) { 
           if (req.readyState == XMLHttpRequest.DONE) {
            var message = req.responseText;
            switch (req.status) {
              case 200:
            	sessionStorage.setItem('username', message); //save username
                window.location.href = "Home.html";			 //redirect to home
                break;
              case 400: // bad request
                document.getElementById("alertMessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("alertMessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("alertMessage").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	
    	 //report to user about form invalid content
    	 password.reportValidity();
    	 username.reportValidity();

      }
  });
  
  //registration submitted event
  document.getElementById("registerButton").addEventListener('click', (e) => { 
    
	  //getting forms reference
	  var username = document.getElementById("registerUsername");
	  var email = document.getElementById("registerEmail");
	  var password1 = document.getElementById("registerPassword1");
	  var password2 = document.getElementById("registerPassword2");
	  
	  var repeatedPasswordOk = (password1.value === password2.value);
	  var emailOk = validateEmail(email.value);
	      
	  //form content validity is checked
	  if (username.checkValidity() && password1.checkValidity() &&
		  email.checkValidity() && password2.checkValidity() && repeatedPasswordOk && email) {
    	
		  //retrieving data from forms and putting them in formData
		  var formData = new FormData();
		  formData.append("username",username.value);
		  formData.append("password1",password1.value);
		  formData.append("password2",password2.value);
		  formData.append("email",email.value);
          
		  //sending form to server
		  makeFormCall("POST", 'Register', formData,
          
		  //callback for response
		  function(req) { 
           if (req.readyState == XMLHttpRequest.DONE) {
            var message = req.responseText;
            switch (req.status) {
              case 200:
            	sessionStorage.setItem('username', message);
                window.location.href = "Home.html";
                break;
              case 400: // bad request
                document.getElementById("alertMessage").textContent = message;
                break;
              case 401: // unauthorized
                  document.getElementById("alertMessage").textContent = message;
                  break;
              case 500: // server error
            	document.getElementById("alertMessage").textContent = message;
                break;
            }
          }
        }
      );
    } else {
    	
    	 //report to user about form invalid content
          password2.reportValidity();
          if(!repeatedPasswordOk) repeatedPasswordOk.setCustomValidity('La password non è stata ripetuta correttamente');
          password1.reportValidity();
          email.reportValidity();
          if(!emailOk) email.setCustomValidity('Email errata');
          username.reportValidity();

    }
  });
  
})();