import Control from 'can-control';
import $ from 'jquery';

import User from 'models/user';
import Country from 'models/country-signup';

import template from './signup.stache';
import "./signup.css";

export default Control.extend({

    "init": function(element, options) {
    var params = {};
    Country.findAll(
      params,
      function (countries) {
        $(element).html(template({
          countries: countries
        }));
        $(element).fadeIn();
      },
      function (response) {
        new ErrorPage(element, response);
      });
	
    $(document).on("click", "#tos-view-btn", function () {
      $("#terms-and-conditions").removeClass("hidden");
    });

    $(document).on("click", "#dpa-view-btn", function () {
      $("#data-processing-agreement").removeClass("hidden");
    });

    $(document).on("click", ".close-btn", function () {
      $("#terms-and-conditions").addClass("hidden");
      $("#data-processing-agreement").addClass("hidden");
    });

    // We use the MutationObserver to detect that the .content under #terms-and-conditions has been added to the DOM
    var observer = new MutationObserver(function (mutations) {
      mutations.forEach(function (mutation) {
        if (mutation.type === "childList" && mutation.addedNodes.length > 0) {
          var contentElement = document.querySelector("#terms-and-conditions .tac-background .content");
          if (contentElement) {
            contentElement.addEventListener("scroll", function () {
              var scrollTop = this.scrollTop;
              var tcHeight = this.clientHeight;
              var scrolled = Math.ceil(scrollTop + tcHeight) + 5;
              var scrollHeight = this.scrollHeight;
              console.log(scrolled);
              console.log(scrollHeight);
              if (scrolled >= scrollHeight) {
                document.querySelector('#accept-terms-and-conditions').disabled = false;
              }
            });
            observer.disconnect();
          }
	    else{
		console.log("TAC element not found");
	    }

          var contentElement = document.querySelector("#data-processing-agreement .tac-background .content");
          if (contentElement) {
            contentElement.addEventListener("scroll", function () {
              var scrollTop = this.scrollTop;
              var tcHeight = this.clientHeight;
              var scrolled = Math.ceil(scrollTop + tcHeight) + 5;
              var scrollHeight = this.scrollHeight;
              console.log(scrolled);
              console.log(scrollHeight);
              if (scrolled >= scrollHeight) {
                document.querySelector('#accept-eu').disabled = false;
              }
            });
            observer.disconnect();
          }
	    else{
		console.log("DPA element not found");
	    }
	    
        }
      });
    });
	observer.observe(document.body, { childList: true, subtree: true });
	console.log("Observer: "+JSON.stringify(observer));
	
	this.emailRequired=true;
    //this.emailRequired = options.appState.attr('emailRequired');
    $(element).hide();
    $(element).html(template({
      emailRequired: options.appState.attr('emailRequired'),
      userEmailDescription: options.appState.attr('userEmailDescription'),
      userWithoutEmailDescription: options.appState.attr('userWithoutEmailDescription')
    }));
    $(element).fadeIn();
    }, // init

  "#anonymous1 click" : function(){
    this.updateEmailControl();
  },

  "#anonymous2 click" : function(){
    this.updateEmailControl();
  },

  "updateEmailControl": function() {
      if (!this.emailRequired){
        var anonymousControl = $(this.element).find("[name='anonymous']:checked");
        var anonymous = (anonymousControl.val() == "1");
        var mail = $(this.element).find("[name='mail']");
        if (anonymous){
          mail.attr('disabled','disabled');
        } else {
          mail.removeAttr('disabled');
        }
      }
   },

  'submit': function(element, event) {
    event.preventDefault();

    var that = this;
    var user = new User();

    // anonymous radiobutton
    var anonymous = false;

    if (!this.emailRequired){
      var anonymousControl = $(element).find("[name='anonymous']:checked");
      anonymous = (anonymousControl.val() == "1");
    }

    // username
    var username = $(element).find("[name='username']");
    var usernameError = user.checkUsername(username.val());
    this.updateControl(username, usernameError);

    // fullname
    var fullname = $(element).find("[name='full-name']");
    var fullnameError = user.checkName(fullname.val());
    this.updateControl(fullname, fullnameError);

    // mail
    var mail = $(element).find("[name='mail']");
    if (!anonymous){
      var mailError = user.checkMail(mail.val());
      this.updateControl(mail, mailError);
    } else {
      this.updateControl(mail, undefined);
    }

    // password
    var newPassword = $(element).find("[name='new-password']");
    var confirmNewPassword = $(element).find("[name='confirm-new-password']");
    var passwordError = user.checkPassword(newPassword.val(), confirmNewPassword.val());
    this.updateControl(newPassword, passwordError);

    if (usernameError || fullnameError || mailError || passwordError) {
      return false;
    }

    $('#save').button('loading');

    $.ajax({
      url: "api/v2/users/register",
      type: "POST",
      data: $(element).find("#signon-form").serialize(),
      dataType: 'json',
      success: function(data) {
        if (data.success == true) {
          // shows success
          var message = "";
          if (!anonymous){
            message = "Well done!</b> An email including the activation code has been sent to your address."
          } else {
            message = "<b>Well done!</b> Your account is now active. <a href=\"/\">Login now</a>."
          }

          $('#signon-form').hide();
          $('#success-message').html(message);
          $('#success-message').show();
        } else {
          // shows error msg
          username = $('#signon-form').find("[name='username']");
          that.updateControl(username, data.message);
          $('#save').button('reset');

        }
      },
      error: function(message) {
        alert('failure: ' + message);
        $('#save').button('reset');
      }
    });

  },

  updateControl: function(control, error) {
    if (error) {
      control.removeClass('is-valid');
      control.addClass('is-invalid');
      control.closest('.form-group').find('.invalid-feedback').html(error);
    } else {
      control.removeClass('is-invalid');
      control.addClass('is-valid');
      control.closest('.form-group').find('.invalid-feedback').html('');
    }
  }

});
