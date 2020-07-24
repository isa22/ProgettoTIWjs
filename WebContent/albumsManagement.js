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

	function PersonalMessage(_username, messagecontainer) {
		this.username = _username;

		//display username
		this.show = function() {
			messagecontainer.textContent = "Username: " + this.username;
		};
		
		
	}



	//List of albums object
	function AlbumsList(_alert, _listcontainer, _listcontainerbody, _extContainer) {

		//reference to alert element
		this.alert = _alert;

		//reference to albums container header
		this.listcontainer = _listcontainer;

		//reference to albums container body
		this.listcontainerbody = _listcontainerbody;

		//reference to external container (used to collapse album area)
		this.externalContainer = _extContainer;

		//clears the html element content and hides it
		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
			this.listcontainerbody.innerHTML="";
			this.externalContainer.removeChild(this.listcontainer);
		};

		//call server for albums data and show them in the page
		this.show = function() {
			var self = this;
			makeSearchCall("GET", "Home",
				function(req) {
					if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						switch (req.status) {
							case 200:
								console.log("Response = " + message);
								var albumsToShow = JSON.parse(req.responseText);
								if (albumsToShow.length == 0) {self.alert.textContent = "No albums found!"; return;}
								self.update(albumsToShow); // self visible by closure
								break;
							case 400: // bad request
								self.alert.textContent = "\u00BB Error code 400: " + message;
								break;
							case 401: // unauthorized
								self.alert.textContent = "\u00BB Error code 401: " + message;
								break;
							case 500: // server error
								self.alert.textContent = "\u00BB Error code 500: " + message;
								break;
						}
					}
				}
			);
		};

		//update the page content about albums
		this.update = function(arrayAlbums) {
			var li, card, cardBody, imgLink, firstImage, title;
			this.listcontainerbody.innerHTML = ""; // empty the card body

			//expand albums area by re-appending the html child elem
			this.externalContainer.appendChild(this.listcontainer);

			// build updated list

			// "self" visible here instead of "this" (this is a trick to make "this" visible in nested functions)
			var self = this;

			arrayAlbums.forEach(function(album) {
				card = document.createElement("div");
				card.setAttribute("sortable", "true");
				card.setAttribute("albumId", album.id);
				card.setAttribute("class", "card mb-4 mx-auto d-block shadow-sm w-50");
				card.setAttribute("id", album.id);
				firstImage = document.createElement("img");
				firstImage.setAttribute("src", getContextPath() + album.firstImagePath);
				firstImage.setAttribute("class", "card-img-top thumbnailsec");
				firstImage.setAttribute('albumId', album.id);
				card.appendChild(firstImage);
				cardBody = document.createElement("div");
				cardBody.setAttribute("class", "card-body");
				cardBody.setAttribute("albumId", album.id);
				card.appendChild(cardBody);
				title = document.createElement("h5");
				title.setAttribute("id", "albumName");
				title.setAttribute("albumId", album.id);
				title.textContent = album.title;
				cardBody.appendChild(title);

				card.addEventListener("click", (e) => {
					// image clicked
					self.reset();
					imagesList.show(e.target.getAttribute("albumId"), 1); // the list must know the details container
				}, false);
				//imgLink.href = "#";
				self.listcontainerbody.appendChild(card);
			});

			//make albums visible
			this.listcontainer.style.visibility = "visible";

			//make albums sortable
			this.sortableAlbums(
				this.listcontainerbody,
				function (sortedAlbum) {
					console.log("album " + sortedAlbum.getAttribute("albumId") + "has changed position");
				}
			);

			//enable save albums order button
			this.setSaveAlbumsOrderButton();
		};

		//make albums sortable by drag and drop
		this.sortableAlbums = function (rootEl, onUpdate) {
			var dragEl;

			console.log("rootEl = " + rootEl.getAttribute("id"));
			console.log(Array.from(rootEl.children));

			// Set all children movable
			Array.from(rootEl.children).forEach(function (itemEl) {
				console.log("itemEl = " + itemEl.getAttribute("id") + " " + itemEl.getAttribute("sortable") );
				itemEl.draggable = true;
			});

			// Function triggered when a dragged element is over an other
			function _onDragOver(e) {

				//disable default event behavior
				e.preventDefault();

				//change cursor icon
				e.dataTransfer.dropEffect = 'move';

				console.log("dragging: " + dragEl.tagName);

				//target that triggered the event (note: it isn't the one that is being dragged)
				var target = e.target;

				//choosing if the destination target is suitable to change its position in the album list
				//with the dragged element
				if( target && target !== dragEl && target.getAttribute("sortable") === "true" ){

					var rect = target.getBoundingClientRect(); //target card rect (used to get its size on screen)
					var next = (e.clientY - rect.top)/(rect.bottom - rect.top) > .5; //true if we are up the target that triggered the event

					//thanks to "next" we understand where to put in the albumList the dragged album
					rootEl.insertBefore(dragEl, next && target.nextSibling || target);

				}
			}

			// End of sorting
			function _onDragEnd(e){

				//disable default event behavior
				e.preventDefault();

				//make dragged element look normal (no more transparent)
				dragEl.classList.remove('ghost');

				//remove drag and drop events listener
				rootEl.removeEventListener('dragover', _onDragOver, false);
				rootEl.removeEventListener('dragend', _onDragEnd, false);

				// Notification about the end of sorting (for debug purpose)
				onUpdate(dragEl);
			}

			//Drag and drop initialization
			rootEl.addEventListener('dragstart', function (evt){

				//making sure to drag the right html element
				var draggingParentOrChild = evt.target.getAttribute("sortable")==="true";

				//storing in "dragEl" the dragged element
				dragEl = (draggingParentOrChild)?evt.target:evt.target.parentNode;

				//Setting the drag effect
				evt.dataTransfer.effectAllowed = 'move';

				//Setting data that are transferred while dropping
				evt.dataTransfer.setData('Text', dragEl.textContent);

				// Subscribing drag and drop events
				rootEl.addEventListener('dragover', _onDragOver, false);
				rootEl.addEventListener('dragend', _onDragEnd, false);

				//timeout with 0 wait seconds is a trick to schedule the callback
				//to be run asynchronously, after the shortest possible delay
				setTimeout(function () {
					dragEl.classList.add('ghost'); //set the dragged object to be transparent
				}, 0)
			}, false);
		};

		//enable save albums order button
		this.setSaveAlbumsOrderButton = function () {

			//retrieve button html reference
			document.getElementById("saveAlbumsOrder").addEventListener("click", (e) => {

				var self=this;
				var albumsIdOrder = [];

				//retrieve album order from html document
				Array.from(this.listcontainerbody.children).forEach(function (child) {
					albumsIdOrder.push(child.getAttribute("albumId"));
				});

				console.log("Sending new albums order to server...");
				console.log(albumsIdOrder);

				//sending new album order toi server
				makeJsonCall("POST", "ChangeAlbumOrder", JSON.stringify({"newOrder":albumsIdOrder}),
					function(req) {
						if (req.readyState == XMLHttpRequest.DONE) {
							var message = req.responseText;
							switch (req.status) {
								case 200:
									console.log("New albums order saved successfully");

									//log in green that the new albums order is saved
									self.alert.classList.remove("text-danger");
									self.alert.classList.add("text-success");
									self.alert.textContent = "\u00BB New albums order saved successfully";

									//clean alert bar after 5 seconds
									setTimeout(function () {
										self.alert.textContent = "\u00BB";
										self.alert.classList.add("text-danger");
										self.alert.classList.remove("text-success");
									},5000);

									break;
								case 400: // bad request
									self.alert.textContent = "\u00BB Error code 400: " + message;
									break;
								case 401: // unauthorized
									self.alert.textContent = "\u00BB Error code 401: " + message;
									break;
								case 500: // server error
									self.alert.textContent = "\u00BB Error code 500: " + message;
									break;
							}
						}
					});
				});
		}
	}

	//album title bar, handles return to albums list event
	function AlbumTitleLine(_albumTitleLine, _albumTitle, _albumList, _imageList){
		this.albumTitleLine = _albumTitleLine;
		this.albumTitlelabel = _albumTitle;
		this.albumList = _albumList;
		this.imageList = _imageList;

		//show album title bar
		this.show = function(albumTitle){
			this.albumTitleLine.style.visibility="visible";
			this.albumTitlelabel.textContent = albumTitle;
		};

		//hide album title bar
		this.reset = function (){
			this.albumTitleLine.style.visibility="hidden";
		};

		//return to album list
		this.setReturnToAlbums = function () {
			document.getElementById("returnToAlbum").addEventListener("click", (e) => {
				this.imageList.reset();
				this.albumList.show();
				this.reset();
			});
		}
	}

	//List of images object
	function ImagesList(_alert,_titleLine, _listcontainer, _listcontainerbody) {

		//reference to alert element
		this.alert = _alert;

		this.titleLine = _titleLine;

		//reference to albums container header
		this.listcontainer = _listcontainer;

		//reference to albums container body
		this.listcontainerbody = _listcontainerbody;

		//clears the html element content and hides it
		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
			this.listcontainerbody.innerHTML="";
			document.getElementById("next").style.visibility="hidden";
			document.getElementById("previous").style.visibility="hidden";
		};

		//loaded album data
		this.currentAlbumImages = null;
		this.currentNumOfPages = null;
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
								break;
							case 400: // bad request
								self.alert.textContent = "\u00BB Error code 400: " + message;
								break;
							case 401: // unauthorized
								self.alert.textContent = "\u00BB Error code 401: " + message;
								break;
							case 500: // server error
								self.alert.textContent = "\u00BB Error code 500: " + message;
								break;
						}
					}
				}

			);
		};

		//registering navigation buttons events
		this.setPaginationButtons = function () {
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

			//rename this to make it visible in inner functions
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
				imgPreview.setAttribute("data-toggle", "modal");
				imgPreview.setAttribute("data-target", "modalImageContainer");
				cardBody = document.createElement("div");
				cardBody.setAttribute("class","card-body");
				imgName = document.createElement("p");
				imgName.textContent = image.title;
				col.appendChild(card);
				card.appendChild(imgPreview);
				card.appendChild(cardBody);
				cardBody.appendChild(imgName);
				card.setAttribute('imageId', image.id); // set a custom HTML attribute
				imgPreview.addEventListener("mouseover", (e) => {
					// dependency via module parameter
					imageDetails.update(image, image.comments); // the list must know the details container
				}, false);
				self.listcontainerbody.appendChild(col);
			});
			this.listcontainer.style.visibility = "visible";
			console.log(this.listcontainer);

			//hide/show buttons
			var previousButton = document.getElementById("previous");
			var nextButton = document.getElementById("next");
			var numOfPages = Math.ceil(self.currentAlbumImages.length/this.imagesPerPage);
			if(page<=1) previousButton.style.visibility = "hidden";
			else previousButton.style.visibility = "visible";
			if(page>=numOfPages) nextButton.style.visibility = "hidden";
			else nextButton.style.visibility = "visible";
		};

	}

	//Image details object
	  function ImageDetails(_alert, _listcontainer, _listcontainerbody) {
		//reference to alert element
		this.alert = _alert;

		//reference to modal container
		this.listcontainer = _listcontainer;

		//reference to modal container body
		this.listcontainerbody = _listcontainerbody;

		this.reset = function() {
			this.listcontainer.style.visibility = "hidden";
		};
		
		
		//update the page content with the image details
		this.update = function(image, comments) {
			
			this.currentImage = image;
			//html elements for showing the images
			var modalContent, modalHeader, modalTitle, closeButton, closeText, modalBody;
			var infoContainer, naturalImg, card, cardBody, cardText;
			var headlineCom, cardCom, cardHeaderCom, author, cardBodyCom, textCom, cardFooterCom, dateCom;
			var titleForm, form, textForm, label, textArea, imageParam, buttonForm;
			
			this.listcontainerbody.innerHTML = ""; // empty image list body
			var self = this;

			//create modal content and set its class
			modalContent = document.createElement("div");
			modalContent.setAttribute("class","modal-content");
			
			//create modal header, set its class and append it to modal content
			modalHeader = document.createElement("div");
			modalHeader.setAttribute("class","modal-header");
			modalContent.appendChild(modalHeader);
			
			//create modal title containing the image title, set its class, set its id and append it to modal header
			modalTitle = document.createElement("h5");
			modalTitle.setAttribute("class","modal-title");
			modalTitle.setAttribute('titleId',"modalTitle");
			modalTitle.textContent = image.title;
			modalHeader.appendChild(modalTitle);
			
			//create the button to close the modal, of type button and append it to modal header
			closeButton = document.createElement("button");
			closeButton.setAttribute("type","button");
			closeButton.setAttribute("class","close");
			closeButton.setAttribute("data-dismiss","modal");
			closeButton.setAttribute("aria-label","Close");
			modalHeader.appendChild(closeButton);
			
			//create the text and append it to button
			closeText = document.createElement("div");
			closeText.setAttribute("aria-hidden","true");
			closeText.textContent = "\u00D7";
			closeButton.appendChild(closeText);
			
			//create the modal body and append it to modal content
			modalBody = document.createElement("div");
			modalBody.setAttribute("class","modal-body");
			modalContent.appendChild(modalBody);
			
			this.listcontainerbody.appendChild(modalContent);
			
			//create the container for the informations that will contain the image, the description and the comments 
			infoContainer = document.createElement("div");
			infoContainer.setAttribute("class","my-sm-2");
			modalBody.appendChild(infoContainer);
			
			//create the image and append to the info container
			naturalImg = document.createElement("img");
			naturalImg.setAttribute("class","my-sm-2 mx-auto d-block");
			naturalImg.setAttribute("src",getContextPath() + image.path);
			naturalImg.setAttribute("alt", image.title);
			naturalImg.setAttribute('imageId', image.id);
			infoContainer.appendChild(naturalImg);
			
			
			card = document.createElement("div");
			card.setAttribute("class","card my-sm-2");
			infoContainer.appendChild(card);
			
			cardBody = document.createElement("div");
			cardBody.setAttribute("class","card-body");
			card.appendChild(cardBody);
			
			cardText = document.createElement("p");
			cardText.setAttribute("class","card-text");
			cardText.textContent = image.description;
			cardBody.appendChild(cardText);
			
			
			if(comments[0] == undefined){
				headlineCom = document.createElement("h5");
				headlineCom.setAttribute("class","mt-sm-5 mb-sm-3");
				headlineCom.textContent = 'Comments';
				cardBody.appendChild(headlineCom);
				
				cardCom = document.createElement("div");
				cardCom.setAttribute("class","card");
				cardBody.appendChild(cardCom);
				
				cardBodyCom  = document.createElement("div");
				cardBodyCom.setAttribute("class","card-body");
				cardBodyCom.textContent = 'No comments';
				cardCom.appendChild(cardBodyCom);
				
			}
			else{
				headlineCom = document.createElement("h5");
				headlineCom.setAttribute("class","mt-sm-5 mb-sm-3");
				headlineCom.textContent = 'Comments';
				cardBody.appendChild(headlineCom);
				
				comments.forEach(function(comment) {
					cardCom = document.createElement("div");
					cardCom.setAttribute("class","card");
					cardBody.appendChild(cardCom);
					
					cardHeaderCom = document.createElement("div");
					cardHeaderCom.setAttribute("class","card-header");
					cardCom.appendChild(cardHeaderCom);
					
					author = document.createElement("strong");
					author.setAttribute("class","d-block text-gray-dark");
					author.textContent = comment.username;
					cardHeaderCom.appendChild(author);
					
					cardBodyCom = document.createElement("div");
					cardBodyCom.setAttribute("class","card-body");
					cardBodyCom.textContent = comment.text;
					cardCom.appendChild(cardBodyCom);
					
					cardFooterCom = document.createElement("div");
					cardFooterCom.setAttribute("class","card-footer");
					cardCom.appendChild(cardFooterCom);
					
					dateCom = document.createElement("span");
					dateCom.setAttribute("class","text-muted small");
					dateCom.textContent = comment.timestamp;
					cardFooterCom.appendChild(dateCom);
					
				});
				
			}
			
			titleForm = document.createElement("h5");
			titleForm.setAttribute("class","mt-sm-5 mb-sm-3");
			titleForm.textContent = "Submit a comment";
			cardBody.appendChild(titleForm);
			
			form = document.createElement("form");
			form.setAttribute("id","form");
			cardBody.appendChild(form);
			
			textForm = document.createElement("div");
			textForm.setAttribute("class","form-group");
			form.appendChild(textForm);
			
			label = document.createElement("label");
			label.setAttribute("for","comment");
			label.textContent = "Comment:";
			textForm.appendChild(label);
			
			textArea = document.createElement("textarea");
			textArea.setAttribute("class","form-control");
			textArea.setAttribute("name","comment");
			textArea.setAttribute("id","comment");
			textArea.setAttribute("rows","5");
			textArea.setAttribute("required", "");
			textForm.appendChild(textArea);
			
			imageParam = document.createElement("input");
			imageParam.setAttribute("type","hidden");
			imageParam.setAttribute("name","imageId");
			imageParam.setAttribute("value", image.id);
			form.appendChild(imageParam);
			
			buttonForm = document.createElement("button");
			buttonForm.setAttribute("class","my-sm-4 btn btn-outline-primary");
			buttonForm.setAttribute("type","button");
			buttonForm.setAttribute("id","button");
			buttonForm.textContent = "Submit Comment";
			form.appendChild(buttonForm);
			
			
			$('#modalImageContainer').modal('show');
			
		    $("#button").click(function(){   
		    	makeFormCall('POST', 'CreateComment', new FormData(form) , 
		    			function(req){
		    		if (req.readyState == XMLHttpRequest.DONE) {
						var message = req.responseText;
						switch (req.status) {
							case 200:
								var comments = JSON.parse(message);
								console.log(comments);
								console.log(self.currentImage);
								image.comments = comments;
								self.update(image, comments);
								break;
							case 400: // bad request
								self.alert.textContent = "\u00BB Error code 400: " + message;
								break;
							case 401: // unauthorized
								self.alert.textContent = "\u00BB Error code 401: " + message;
								break;
							case 500: // server error
								self.alert.textContent = "\u00BB Error code 500: " + message;
								break;
						}
					}
		    				
		    	})
		        
		    }); 
		    
		};

	  }

     //Organize and init all html elements and js objects after page load
	function PageOrchestrator() {

		//create all object that manages the page behaviour
		var alertContainer = document.getElementById("alertMessage");
		this.start = function() {

			var personalMessage = new PersonalMessage(sessionStorage.getItem('username'),
				document.getElementById("personalMessage"));
			personalMessage.show();

			albumList = new AlbumsList(
				alertContainer,
				document.getElementById("albumsContainer"),
				document.getElementById("albumsBody"),
				document.getElementById("albumsExternalContainer")
				);

			titleLine = new AlbumTitleLine(document.getElementById("albumTitleLine"),
				document.getElementById("albumTitle"), albumList, null);
			titleLine.reset(); //hide album title line

			imagesList = new ImagesList(
				alertContainer,
				titleLine,
				document.getElementById("imagesContainer"),
				document.getElementById("imagesBody"));
			

			titleLine.imageList = imagesList; //set image list to titleLine

			titleLine.setReturnToAlbums();	  //initialize return to albums button

			imagesList.setPaginationButtons(); //initialize pagination buttons

			imageDetails = new ImageDetails(
					alertContainer, 
					document.getElementById("modalImageContainer"),
					document.getElementById("modalImageBody"));
			
			document.getElementById("logoutButton").addEventListener('click', () => {
		        sessionStorage.clear();
		      })
		};

		//bring page to default state
		this.refresh = function() {
			alertContainer.textContent = "\u00BB";
			imagesList.reset();
			albumList.show();
			//imageDetails.reset();
		};
	}
})();
