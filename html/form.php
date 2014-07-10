<!DOCTYPE html>
<html lang="en">
  <head>
    <title>Cheetah</title>
    <script type="text/javascript" src="jquery-2.1.1.js"></script>

    <script>
      $(document).ready(function() {
          $('form').submit(function(event) { //Trigger on form submit
            $('#name + .throw_error').empty(); //Clear the messages first
            $('#success').empty();
        
            var postForm = { //Fetch form data
              'name'   : $('input[name=name]').val() //Store name fields value
            };
        
            $.ajax({ //Process the form using $.ajax()
              type     : 'POST', //Method type
              url     : 'process.php', //Your form processing file url
              data     : postForm, //Forms name
              dataType   : 'json',
              success   : function(data) {
                
              if (!data.success) { //If fails
                if (data.errors.name) { //Returned if any error from process.php
                  $('.throw_error').fadeIn(1000).html(data.errors.name); //Throw relevant error
                 }
               } else {
                  $('#success').fadeIn(1000).append('<p>' + data.posted + '</p>'); //If successful, than throw a success message
                }
              }
            });
              event.preventDefault(); //Prevent the default submit
          });
        });
    </script>
    <style>
      ul {
        font-family: Arial;
        list-style-type: none;
      }

      #success {
        display: none;
        font-family: Arial;
        color: green;
        margin-left: 85px;
        font-size: 14px;
      }

      input[type=text] {
        padding: 5px;
        margin-left: 35px;
        box-shadow: inset 0 0 5px #eee;
        border: 1px solid #eee;
      }

      input[type=submit] {
        padding: 3px 8px;
        background: #eee;
        margin-left: 85px;
        cursor: pointer;
        border: 1px solid #aaa;
        font-size: 12px;
      }

      .throw_error {
        color:tomato;
        font-size: 12px;
        display: none;
      }

      label {
        font-size: 13px;
      }
    </style>
  </head>
  <body>
    <h1 align="center">CHEETAH</h1>
    <form align="center" method="post" name="postForm">
      <ul>
        <li>
          <input align="middle"  type="text" name="name" id="name" size="99" />
          <span class="throw_error"></span>
        </li>
      </ul>
      <input type="submit" value="Run" />
    </form>
    <div id="success"></div>
  </body>
</html>

