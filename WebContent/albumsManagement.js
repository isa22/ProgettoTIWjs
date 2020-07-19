	(function() { // avoid variables ending up in the global scope

	  // page components
	  var imageDetails, imagesList, albumList,
	    pageOrchestrator = new PageOrchestrator(); // main controller

	  window.addEventListener("load", () => {
	    if (sessionStorage.getItem("username") == null) {
	      window.location.href = "Login.html"; //redirect if not logged
	    } else {
	      pageOrchestrator.start(); // initialize the components
	      pageOrchestrator.refresh();
	    } // display initial content
	  }, false);


	  // Constructors of view components

	  function PersonalMessage(_username, messagecontainer) { //TODO added by prof, actually I don't know the its purpose
	    this.username = _username;
	    this.show = function() {
	      messagecontainer.textContent = this.username;
	    }
	  }

	  //List of albums object
	  function AlbumsList(_alert, _listcontainer, _listcontainerbody) {

	  	//reference to alert element
	  	this.alert = _alert;

	  	//reference to albums container header //TODO may not be useful
	    this.listcontainer = _listcontainer;

	    //reference to albums container body
	    this.listcontainerbody = _listcontainerbody;

	    //hide the html element
	    this.reset = function() {
	      this.listcontainer.style.visibility = "hidden";
	    };

	    //call server for albums data and show them in the page
	    this.show = function(next) {
	      var self = this;
	      makeSearchCall("GET", "GetHomePage", null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var albumsToShow = JSON.parse(req.responseText);
	              if (albumsToShow.length == 0) {
	                self.alert.textContent = "No albums found!";
	                return;
	              }
	              self.update(albumsToShow); // self visible by closure
	              if (next) next(); // show the default element of the list if present
	            }
	          } else {
				  self.alert.textContent = message;
			  }
	        }
	      );
	    };

		//update the page content about albums
	    this.update = function(arrayAlbums) {
	      var card, cardClass, imgLink, firstImage, body, name;
	      this.listcontainerbody.innerHTML = ""; // empty the card body
	      // build updated list
	      var self = this;
	      arrayAlbums.forEach(function(album) { // self visible here, not this
	        card = document.createElement("div");
	        card.setAttribute("class", "card mb-4 shadow-sm");
	        imgLink = document.createElement("a");
	        card.appendChild(imgLink);
	        imgLink.setAttribute('albumId', album.id);
	        firstImage = document.createElement("img");
	        firstImage.setAttribute("src", album.firstImagePath);
	        firstImage.setAttribute("class", "card-img-top thumbnailsec");
	        firstImage.setAttribute("id", "albumImage");
	        imgLink.appendChild(firstImage);
	        cardbody = document.createElement("div");
	        cardbody.setAttribute("class", "card-body");
	        card.appendChild(body);
	        title = document.createElement("h5");
	        title.setAttribute("id", "albumName");
	        title.textContent = album.title;
	        body.appendChild(title);
	        

	        //TODO finish the html element
	        //...

			  
			  card.addEventListener("click", (e) => {
	          // image clicked
	          imageDetails.show(e.target.getAttribute("albumId")); // the list must know the details container
	        }, false);
	        imgLink.href = "#";
	        self.listcontainerbody.appendChild(card);
	      });
	      this.listcontainer.style.visibility = "visible";

	    };

	    //TODO understand what this function does
	    this.autoclick = function(albumId) {
	      var e = new Event("click");
	      var selector = "a[albumId='" + albumId + "']";
	      var anchorToClick =
	        (albumId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
	      if (anchorToClick) anchorToClick.dispatchEvent(e);
	    }

	  }

		//List of images object //TODO uncompleted object
		function ImagesList(_alert, _listcontainer, _listcontainerbody) {

	  	//reference to alert element
			this.alert = _alert;

			//reference to albums container header //TODO may not be useful
			this.listcontainer = _listcontainer;

			//reference to albums container body
			this.listcontainerbody = _listcontainerbody;

			this.reset = function() {
				this.listcontainer.style.visibility = "hidden";
			};

			//call server for album images and show them in the page
			this.show = function(next) {
				var self = this;
				makeSearchCall("GET", "GetAlbum", null,
					function(req) {
						if (req.readyState == 4) {
							var message = req.responseText;
							if (req.status == 200) {
								var albumImages = JSON.parse(req.responseText);
								if (albumImages.length == 0) {
									self.alert.textContent = "No images yet!";
									return;
								}
								self.update(albumImages.images, 1); // self visible by closure
								if (next) next(); // show the default element of the list if present
							}
						} else {
							self.alert.textContent = message;
						}
					}
				);
			};

			//update the page content about albums
			this.update = function(arrayImages, page) {

				//html elements for showing the images
				var card, imgPreview, cardBody, imgName;

				//images per page
				const imagesPerPage = 5;

				this.listcontainerbody.innerHTML = ""; // empty image list body
				var self = this;

				//arrayImages offsets
				var startImageOffset = (page-1)*imagesPerPage;
				var endImageOffset = startImageOffset + 5;

				//showing 5 page images
				arrayImages.slice(startImageOffset,endImageOffset).forEach(function(image) { // self visible here, not this
					card = document.createElement("div");
					card.setAttribute("class","card mb-4 shadow-sm");
					imgPreview = document.createElement("img");
					imgPreview.setAttribute("class","card-img-top thumbnailsec");
					imgPreview.setAttribute("src",image.path);
					cardBody = document.createElement("div");
					cardBody.setAttribute("class","card-body");
					imgName = document.createElement("p");
					imgName.textContent = image.title;
					card.appendChild(imgPreview);
					card.appendChild(cardBody);
					cardBody.appendChild(imgName);
					card.setAttribute('imageId', image.id); // set a custom HTML attribute
					card.addEventListener("click", (e) => { //TODO add the event for cursor on image
						// dependency via module parameter
						imageDetails.show({
							title: image.title,
							description: image.description,
							comments: image.comments
						}); // the list must know the details container
					}, false);
					self.listcontainerbody.appendChild(card);
				});
				this.listcontainer.style.visibility = "visible";

			};

			//TODO understand what this function does
			this.autoclick = function(albumId) {
				var e = new Event("click");
				var selector = "a[albumid='" + albumId + "']";
				var anchorToClick =
					(albumId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
				if (anchorToClick) anchorToClick.dispatchEvent(e);
			}

		}

	  //Image details object //TODO uncompleted object
	  function ImageDetails(options) {
	    this.alert = options['alert'];
	    this.descriptioncontainer = options['descriptioncontainer'];
	    this.expensecontainer = options['expensecontainer'];
	    this.expenseform = options['expenseform'];
	    this.closeform = options['closeform'];
	    this.date = options['date'];
	    this.destination = options['destination'];
	    this.status = options['status'];
	    this.description = options['description'];
	    this.country = options['country'];
	    this.province = options['province'];
	    this.city = options['city'];
	    this.fund = options['fund'];
	    this.food = options['food'];
	    this.accomodation = options['accomodation'];
	    this.travel = options['transportation'];

	    this.registerEvents = function(orchestrator) {
	      this.expenseform.querySelector("input[type='button']").addEventListener('click', (e) => {
	        var form = e.target.closest("form");
	        if (form.checkValidity()) {
	          var self = this,
	            missionToReport = form.querySelector("input[type = 'hidden']").value;
	          makeCall("POST", 'CreateExpensesReport', form,
	            function(req) {
	              if (req.readyState == 4) {
	                var message = req.responseText;
	                if (req.status == 200) {
	                  orchestrator.refresh(missionToReport);
	                } else {
	                  self.alert.textContent = message;
	                }
	              }
	            }
	          );
	        } else {
	          form.reportValidity();
	        }
	      });

	      this.closeform.querySelector("input[type='button']").addEventListener('click', (event) => {
	        var self = this,
	          form = event.target.closest("form"),
	          missionToClose = form.querySelector("input[type = 'hidden']").value;
	        makeCall("POST", 'CloseMission', form,
	          function(req) {
	            if (req.readyState == 4) {
	              var message = req.responseText;
	              if (req.status == 200) {
	                orchestrator.refresh(missionToClose);
	              } else {
	                self.alert.textContent = message;
	              }
	            }
	          }
	        );
	      });
	    };


	    this.show = function(missionid) {
	      var self = this;
	      makeCall("GET", "GetMissionDetailsData?missionid=" + missionid, null,
	        function(req) {
	          if (req.readyState == 4) {
	            var message = req.responseText;
	            if (req.status == 200) {
	              var mission = JSON.parse(req.responseText);
	              self.update(mission); // self is the object on which the function
	              // is applied
	              self.descriptioncontainer.style.visibility = "visible";
	              switch (mission.status) {
	                case "OPEN":
	                  self.expensecontainer.style.visibility = "hidden";
	                  self.expenseform.style.visibility = "visible";
	                  self.expenseform.missionid.value = mission.id;
	                  self.closeform.style.visibility = "hidden";
	                  break;
	                case "REPORTED":
	                  self.expensecontainer.style.visibility = "visible";
	                  self.expenseform.style.visibility = "hidden";
	                  self.closeform.missionid.value = mission.id;
	                  self.closeform.style.visibility = "visible";
	                  break;
	                case "CLOSED":
	                  self.expensecontainer.style.visibility = "visible";
	                  self.expenseform.style.visibility = "hidden";
	                  self.closeform.style.visibility = "hidden";
	                  break;
	              }
	            } else {
	              self.alert.textContent = message;

	            }
	          }
	        }
	      );
	    };


	    this.reset = function() {
	      this.albumscontainer.style.visibility = "hidden";
	      this.expensecontainer.style.visibility = "hidden";
	      this.expenseform.style.visibility = "hidden";
	      this.closeform.style.visibility = "hidden";
	    };

	    this.update = function(m) {
	      this.date.textContent = m.startDate;
	      this.destination.textContent = m.destination;
	      this.status.textContent = m.status;
	      this.description.textContent = m.description;
	      this.country.textContent = m.country;
	      this.province.textContent = m.province;
	      this.city.textContent = m.city;
	      this.fund.textContent = m.fund;
	      this.food.textContent = m.expenses.food;
	      this.accomodation.textContent = m.expenses.accomodation;
	      this.travel.textContent = m.expenses.transportation;
	    }
	  }

	   //TODO uncompleted object //TODO understand what this object does
	  function Wizard(wizardId, alert) {
	    // minimum date the user can choose, in this case now and in the future
	    var now = new Date(),
	      formattedDate = now.toISOString().substring(0, 10);
	    this.wizard = wizardId;
	    this.alert = alert;

	    this.wizard.querySelector('input[type="date"]').setAttribute("min", formattedDate);

	    this.registerEvents = function(orchestrator) {
	      // Manage previous and next buttons
	      Array.from(this.wizard.querySelectorAll("input[type='button'].next,  input[type='button'].prev")).forEach(b => {
	        b.addEventListener("click", (e) => { // arrow function preserve the
	          // visibility of this
	          var eventfieldset = e.target.closest("fieldset"),
	            valid = true;
	          if (e.target.className == "next") {
	            for (i = 0; i < eventfieldset.elements.length; i++) {
	              if (!eventfieldset.elements[i].checkValidity()) {
	                eventfieldset.elements[i].reportValidity();
	                valid = false;
	                break;
	              }
	            }
	          }
	          if (valid) {
	            this.changeStep(e.target.parentNode, (e.target.className === "next") ? e.target.parentNode.nextElementSibling : e.target.parentNode.previousElementSibling);
	          }
	        }, false);
	      });

	      // Manage submit button
	      this.wizard.querySelector("input[type='button'].submit").addEventListener('click', (e) => {
	        var eventfieldset = e.target.closest("fieldset"),
	          valid = true;
	        for (i = 0; i < eventfieldset.elements.length; i++) {
	          if (!eventfieldset.elements[i].checkValidity()) {
	            eventfieldset.elements[i].reportValidity();
	            valid = false;
	            break;
	          }
	        }

	        if (valid) {
	          var self = this;
	          makeCall("POST", 'CreateMission', e.target.closest("form"),
	            function(req) {
	              if (req.readyState == XMLHttpRequest.DONE) {
	                var message = req.responseText; // error message or mission id
	                if (req.status == 200) {
	                  orchestrator.refresh(message); // id of the new mission passed
	                } else {
	                  self.alert.textContent = message;
	                  self.reset();
	                }
	              }
	            }
	          );
	        }
	      });
	      // Manage cancel button
	      this.wizard.querySelector("input[type='button'].cancel").addEventListener('click', (e) => {
	        e.target.closest('form').reset();
	        this.reset();
	      });
	    };

	    this.reset = function() {
	      var fieldsets = document.querySelectorAll("#" + this.wizard.id + " fieldset");
	      fieldsets[0].hidden = false;
	      fieldsets[1].hidden = true;
	      fieldsets[2].hidden = true;

	    };

	    this.changeStep = function(origin, destination) {
	      origin.hidden = true;
	      destination.hidden = false;
	    }
	  }

	  //TODO uncompleted object
	  function PageOrchestrator() {
	    var alertContainer = document.getElementById("alertMessage");
	    this.start = function() {
	      var personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
	        document.getElementById("personalMessage"));
	      personalMessage.show();

	      albumList = new AlbumsList(
			alertContainer,
			document.getElementById("albumsContainer"),
			document.getElementById("albumsBody"));

	      imagesList = new ImagesList(
	        alertContainer,
	        document.getElementById("imagesContainer"),
	        document.getElementById("imagesBody"));

	      /*imageDetails = new ImageDetails({ // many parameters, wrap them in an
	        // object
	        alert: alertContainer,
	        descriptioncontainer: document.getElementById("descriptioncontainer"),
	        commentscontainer: document.getElementById("commentscontainer"),
	        commentform: document.getElementById("commentform"),
	        date: document.getElementById("date"),
	        destination: document.getElementById("id_destination"),
	        status: document.getElementById("id_status"),
	        description: document.getElementById("id_description"),
	        country: document.getElementById("id_country"),
	        province: document.getElementById("id_province"),
	        city: document.getElementById("id_city"),
	        fund: document.getElementById("id_fund"),
	        food: document.getElementById("id_food"),
	        accomodation: document.getElementById("id_accomodation"),
	        transportation: document.getElementById("id_transportation")
	      });
	      imageDetails.registerEvents(this);*/

	      /*document.querySelector("a[href='Logout']").addEventListener('click', () => {
	        window.sessionStorage.removeItem('username');
	      })*/
	    };


	    this.refresh = function(currentMission) {
	      alertContainer.textContent = "";
	      imagesList.reset();
	      //imageDetails.reset();
	      imagesList.show(function() {
	        imagesList.autoclick(currentMission);
	      }); // closure preserves visibility of this
	    };
	  }
	})();
