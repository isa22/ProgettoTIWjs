(function() { // avoid variables ending up in the global scope

	// page components
	var imageDetails, imagesList, albumList, titleLine,
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
			messagecontainer.textContent = "Username: " + this.username;
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

		//clears the html element content
		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
			this.listcontainerbody.innerHTML="";
		};

		//call server for albums data and show them in the page
		this.show = function(next) {
			var self = this;
			makeSearchCall("GET", "Home",
				/*function(req) {
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
                }*/
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						switch (req.status) {
							case 200:
								console.log("Response = " + message);
								var albumsToShow = JSON.parse(req.responseText);
								if (albumsToShow.length == 0) {self.alert.textContent = "No albums found!"; return;}
								self.update(albumsToShow); // self visible by closure
								if (next) next(); // show the default element of the list if present
								break;
							case 400: // bad request
								self.alert.textContent = message;
								break;
							case 401: // unauthorized
								self.alert.textContent = message;
								break;
							case 500: // server error
								self.alert.textContent = message;
								break;
						}
					}
				}
			);
		};

		//update the page content about albums
		this.update = function(arrayAlbums) {
			var card, cardBody, imgLink, firstImage, title;
			this.listcontainerbody.innerHTML = ""; // empty the card body
			// build updated list
			var self = this;


			/*function sortable(rootEl, onUpdate) {
				var dragEl;

				// Making all siblings movable
				[].slice.call(rootEl.children).forEach(function (itemEl) {
					itemEl.draggable = true;
				});

				// Function responsible for sorting
				function _onDragOver(evt) {
					evt.preventDefault();
					evt.dataTransfer.dropEffect = 'move';

					var target = evt.target;
					if (target && target !== dragEl && target.nodeName == 'LI') {
						// Sorting
						rootEl.insertBefore(dragEl, target.nextSibling || target);
					}
				}

				// End of sorting
				function _onDragEnd(evt){
					evt.preventDefault();

					dragEl.classList.remove('ghost');
					rootEl.removeEventListener('dragover', _onDragOver, false);
					rootEl.removeEventListener('dragend', _onDragEnd, false);


					// Notification about the end of sorting
					onUpdate(dragEl);
				}

				// Sorting starts
				rootEl.addEventListener('dragstart', function (evt){
					dragEl = evt.target; // Remembering an element that will be moved

					// Limiting the movement type
					evt.dataTransfer.effectAllowed = 'move';
					evt.dataTransfer.setData('Text', dragEl.textContent);


					// Subscribing to the events at dnd
					rootEl.addEventListener('dragover', _onDragOver, false);
					rootEl.addEventListener('dragend', _onDragEnd, false);


					setTimeout(function () {
						// If this action is performed without setTimeout, then
						// the moved object will be of this class.
						dragEl.classList.add('ghost');
					}, 0)
				}, false);

				// Using
				sortable(arrayAlbums, function (album) {
					console.log(album);
				});
			}*/

			arrayAlbums.forEach(function(album) { // self visible here, not this
				card = document.createElement("div");
				card.setAttribute("class", "card mb-4 mx-auto d-block shadow-sm w-50");
				imgLink = document.createElement("a");
				card.setAttribute("id", album.id);
				card.appendChild(imgLink);
				imgLink.setAttribute('albumId', album.id);
				firstImage = document.createElement("img");
				firstImage.setAttribute("src", getContextPath() + album.firstImagePath);
				firstImage.setAttribute("class", "card-img-top thumbnailsec");
				firstImage.setAttribute('albumId', album.id);
				imgLink.appendChild(firstImage);
				cardBody = document.createElement("div");
				cardBody.setAttribute("class", "card-body");
				card.appendChild(cardBody);
				title = document.createElement("h5");
				title.setAttribute("id", "albumName");
				title.textContent = album.title;
				cardBody.appendChild(title);

				firstImage.addEventListener("click", (e) => {
					// image clicked
					self.reset();
					imagesList.show(e.target.getAttribute("albumId"), 1); // the list must know the details container
				}, false);
				imgLink.href = "#";
				self.listcontainerbody.appendChild(card);
			});
			this.listcontainer.style.visibility = "visible";

		};

	}

	//album title bar, handles return to albums list event
	function AlbumTitleLine(_albumTitleLine, _albumTitle, _albumList, _imageList){
		this.albumTitleLine = _albumTitleLine;
		this.albumTitlelabel = _albumTitle;
		this.albumList = _albumList;
		this.imageList = _imageList;
		this.show = function(albumTitle){
			this.albumTitleLine.style.visibility="visible";
			this.albumTitlelabel.textContent = albumTitle;
		};
		this.reset = function (){
			this.albumTitleLine.style.visibility="hidden";
			//this.albumTitleLine.innerHTML="";
		};
		this.setReturnToAlbums = function () {
			document.getElementById("returnToAlbum").addEventListener("click", (e) => {
				this.imageList.reset();
				this.albumList.show();
				this.reset();
			});
		}
	}

	//List of images object //TODO uncompleted object
	function ImagesList(_alert,_titleLine, _listcontainer, _listcontainerbody) {

		//reference to alert element
		this.alert = _alert;

		this.titleLine = _titleLine;

		//reference to albums container header //TODO may not be useful
		this.listcontainer = _listcontainer;

		//reference to albums container body
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
			this.listcontainerbody.innerHTML="";
			document.getElementById("next").style.visibility="hidden";
			document.getElementById("previous").style.visibility="hidden";
		};

		//loaded album data
		this.currentAlbumImages = null;
		this.currentNumOfPages = null;
		this.currentAlbumId = null;
		this.currentAlbumTitle = "albumTitle";
		this.currentPage = 1;
		this.imagesPerPage = 5; //images per page

		//call server for album images and show them in the page
		this.show = function(albumId) {
			var self = this;
			//download album images
			makeSearchCall("GET", "Album?albumId="+albumId,
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						switch (req.status) {
							case 200:
								var albumImages = JSON.parse(message);
								if (albumImages.length == 0) {
									self.alert.textContent = "No images yet!";
									return;
								}
								self.currentAlbumTitle = albumImages.title;
								self.update(albumImages.images, 1); // self visible by closure
								//if (next) next(); // show the default element of the list if present
								break;
							case 400: // bad request
								self.alert.textContent = message;
								break;
							case 401: // unauthorized
								self.alert.textContent = message;
								break;
							case 500: // server error
								self.alert.textContent = message;
								break;
						}
					}
				}

			);
		};

		//registering navigation buttons events
		this.setPaginationButtons = function () {
			/*console.log("Num of pages: " + numOfPages);
            console.log("Num of images: " + this.currentAlbumImages.length);
            console.log("images per page: " + this.imagesPerPage);*/
			console.log("Current page: " + this.currentPage);

			var previousButton = document.getElementById("previous");
			var nextButton = document.getElementById("next");
			previousButton.addEventListener("click", (e) => {
				if(this.currentPage>1) {
					var previousPage = this.currentPage-1;
					console.log("Go to previous page " + previousPage +" from page "+ this.currentPage);
					this.update(this.currentAlbumImages,previousPage); //go to previous page
				}
			}, false);
			nextButton.addEventListener("click", (e) => {
				if(this.currentPage<this.currentNumOfPages) {
					var nextPage = this.currentPage + 1;
					console.log("Go to next page " + nextPage + " from page " + this.currentPage);
					this.update(this.currentAlbumImages, nextPage); //go to next page
				}
			}, false);
		};

		//update the page content about albums
		this.update = function(arrayImages, page) {

			//rename this
			var self = this;

			//store current album data
			this.currentAlbumImages = arrayImages; //store array of images
			this.currentPage = page;
			this.currentNumOfPages = Math.ceil(self.currentAlbumImages.length/this.imagesPerPage);

			//html elements for showing the images
			var card, imgPreview, cardBody, imgName, col;

			this.listcontainerbody.innerHTML = ""; // empty image list body

			//show album title line
			this.titleLine.show(this.currentAlbumTitle);

			//arrayImages offsets
			var startImageOffset = (page-1)*this.imagesPerPage;
			var endImageOffset = startImageOffset + 5;

			//showing 5 page images
			arrayImages.slice(startImageOffset,endImageOffset).forEach(function(image) { // self visible here, not this
				col = document.createElement("div");
				col.setAttribute("class","col-md-5ths col-xs-6");
				card = document.createElement("div");
				card.setAttribute("class","card mb-4 shadow-sm");
				imgPreview = document.createElement("img");
				imgPreview.setAttribute("class","card-img-top thumbnailsec");
				imgPreview.setAttribute("src",getContextPath() + image.path);
				cardBody = document.createElement("div");
				cardBody.setAttribute("class","card-body");
				imgName = document.createElement("p");
				imgName.textContent = image.title;
				col.appendChild(card);
				card.appendChild(imgPreview);
				card.appendChild(cardBody);
				cardBody.appendChild(imgName);
				card.setAttribute('imageId', image.id); // set a custom HTML attribute
				imgPreview.addEventListener("mouseover", (e) => { //TODO add the event for cursor on image
					// dependency via module parameter
					imageDetails.update({
						image: image
					}); // the list must know the details container
				}, false);
				self.listcontainerbody.appendChild(col);
			});
			this.listcontainer.style.visibility = "visible";

			//hide/show buttons
			var previousButton = document.getElementById("previous");
			var nextButton = document.getElementById("next");
			var numOfPages = Math.ceil(self.currentAlbumImages.length/this.imagesPerPage);
			if(page<=1) previousButton.style.visibility = "hidden";
			else previousButton.style.visibility = "visible";
			if(page>=numOfPages) nextButton.style.visibility = "hidden";
			else nextButton.style.visibility = "visible";
		};

		//TODO understand what this function does
		this.autoclick = function(missionId) {
			var e = new Event("click");
			var selector = "a[missionid='" + missionId + "']";
			var anchorToClick =
				(missionId) ? document.querySelector(selector) : this.listcontainerbody.querySelectorAll("a")[0];
			if (anchorToClick) anchorToClick.dispatchEvent(e);
		}

	}

	//Image details object //TODO uncompleted object
	  function ImageDetails(_alert, _listcontainer, _listcontainerbody) {
		//reference to alert element
		this.alert = _alert;

		//reference to  //TODO may not be useful
		this.listcontainer = _listcontainer;

		//reference to albums container body
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		};
		
		
		//update the page content with the image details
		this.update = function(image) {

			console.log(image);

			//html elements for showing the images
			var modalContent, modalHeader, modalTitle, closeButton, span, modalBody;
			var infoContainer, naturalImg, card, cardBody, cardText;
			

			this.listcontainerbody.innerHTML = ""; // empty image list body
			var self = this;


			modalContent = document.createElement("div");
			modalContent.setAttribute("class","modal-content");
			
			modalHeader = document.createElement("div");
			modalHeader.setAttribute("class","modal-header");
			modalContent.appendChild(modalHeader);
			
			modalTitle = document.createElement("h5");
			modalTitle.setAttribute("class","modal-title");
			modalTitle.setAttribute('titleId',"modalTitle");
			modalTitle.textContent = image.title;
			modalHeader.appendChild(modalTitle);
			
			
			closeButton = document.createElement("button");
			closeButton.setAttribute("type","button");
			closeButton.setAttribute("class","close");
			closeButton.setAttribute("data-dismiss","modal");
			closeButton.setAttribute("aria-label","Close");
			modalHeader.appendChild(closeButton);
			
			span = document.createElement("div");
			span.setAttribute("aria-hidden","true");
			span.textContent = '&times;'
			closeButton.appendChild(span);
			
			modalBody = document.createElement("div");
			modalBody.setAttribute("class","modal-body");
			modalContent.appendChild(modalBody);
			
			self.listcontainerbody.appendChild(modalContent);
			
			/*arrayImages.slice(startImageOffset,endImageOffset).forEach(function(image) { // self visible here, not this
				card = document.createElement("div");
				card.setAttribute("class","card mb-4 shadow-sm");
				imgPreview = document.createElement("img");
				imgPreview.setAttribute("class","card-img-top thumbnailsec");
				imgPreview.setAttribute("src",getContextPath() + image.path);
				imgPreview.setAttribute('imageId', image.id);
				cardBody = document.createElement("div");
				cardBody.setAttribute("class","card-body");
				imgName = document.createElement("p");
				imgName.textContent = image.title;
				card.appendChild(imgPreview);
				card.appendChild(cardBody);
				cardBody.appendChild(imgName);
				card.setAttribute('imageId', image.id); // set a custom HTML attribute
				imgPreview.addEventListener("mouseover", (e) => { //TODO add the event for cursor on image
					// dependency via module parameter
					imageDetails.show({
						title: image.title,
						description: image.description,
						comments: image.comments
					}); // the list must know the details container
				}, false);
				self.listcontainerbody.appendChild(modalContent);
			});*/
			this.listcontainer.style.visibility = "visible";
			

		};
		
		

	
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

			titleLine = new AlbumTitleLine(document.getElementById("albumTitleLine"),
				document.getElementById("albumTitle"), albumList, null);
			titleLine.reset(); //hide album title line

			imagesList = new ImagesList(
				alertContainer,
				titleLine,
				document.getElementById("imagesContainer"),
				document.getElementById("imagesBody"));
			

			titleLine.imageList = imagesList; //set image list to titleLine

			titleLine.setReturnToAlbums();

			imagesList.setPaginationButtons(); //loading pagination buttons

			imageDetails = new ImageDetails(
					alertContainer, 
					document.getElementById("modalImageContainer"),
					document.getElementById("modalImageBody"));
            

			/*document.querySelector("a[href='Logout']").addEventListener('click', () => {
              window.sessionStorage.removeItem('username');
            })*/
		};


		this.refresh = function(currentMission) {
			alertContainer.textContent = "";
			imagesList.reset();
			albumList.show();
			//imageDetails.reset();
			/*imagesList.show(function() {
              imagesList.autoclick(currentMission);
            });*/ // closure preserves visibility of this
		};
	}
})();
