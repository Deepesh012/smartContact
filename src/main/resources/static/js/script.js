console.log("this is script file")

	const toggleSidebar = () => {
		if($(".sidebar").is(":visible")){
			
			$(".sidebar").css("display", "none");
			$(".content").css("margin-left","0%")
			
		}else{
			$(".sidebar").css("display", "block");
			$(".content").css("margin-left","20%")
		}
	};

	function toggleDarkMode() {
	  const body = document.body;
	  const icon = document.getElementById("theme-icon");

	  body.classList.toggle("dark-mode");

	  if (body.classList.contains("dark-mode")) {
	    icon.classList.remove("fa-moon");
	    icon.classList.add("fa-sun");
	  } else {
	    icon.classList.remove("fa-sun");
	    icon.classList.add("fa-moon");
	  }
	}
