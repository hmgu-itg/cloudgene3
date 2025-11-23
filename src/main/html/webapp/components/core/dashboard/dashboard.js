import $ from 'jquery';
import Control from 'can-control';
import stache from 'can-stache';

import Counter from 'models/counter';
import News from 'models/news';

export default Control.extend({

  "init": function(element, options) {
      var url = 'static/home.stache';
      console.log("In dashboard.js");
    $.get(url,
      function(data) {

        var template = stache(data);

        Counter.findOne({}, function(counter) {
          $(element).html(template({
            counter: counter,
            loggedIn: options.appState.loggedIn
          }));
        }, function(message) {
          $(element).html(template({
            counter: undefined,
            loggedIn: options.loggedIn
          }));
        });

        // News.findOne({}, function(news) {
        //   $(element).html(template({
        //     news: news
        //   }));
        // }, function(message) {
        //   $(element).html(template({
        //     news: message
        //   }));
        // });

	  

      });
  }
});
