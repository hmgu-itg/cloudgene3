import Model from 'can-connect/can/model/model';

export default Model.extend({
    findAll: 'GET /api/v2/users/countries'
}, {

  define: {
    'readOnly': {
      get: function() {
        return this.attr('username') === 'admin' || this.attr('username') === 'public';
      }
    }
  }
});
