<!DOCTYPE html>
<html>
<head>
    <title>MOLGENIS</title>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="icon" href="/img/favicon.ico" type="image/x-icon">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
    <script
      src="http://code.jquery.com/jquery-2.2.4.min.js"
      integrity="sha256-BbhdlvQf/xTY9gja0Dq3HiwQF8LaCRTXxZKRutelT44="
      crossorigin="anonymous"></script>
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
</head>
<body>
    <div class="container">
        <form class="form-horizontal" method="post" action="/initial-configuration">
        <h1>Welcome to MOLGENIS!</h1>
        <p>In order to get started we need some information to complete your set-up.</p>
        <br>
          <legend>PostgreSQL configuration</legend>
          <div class="form-group">
            <label for="inputHostName" class="col-sm-3 control-label">Host name *</label>
            <div class="col-sm-9">
              <input type="text" class="form-control" id="inputHostName" name="hostname" value="localhost" required>
            </div>
          </div>
          <div class="form-group">
            <label for="inputPort" class="col-sm-3 control-label">Port *</label>
            <div class="col-sm-9">
              <input type="number" class="form-control" id="inputPort" name="port" value="5432" required>
            </div>
          </div>
          <div class="form-group">
            <label for="inputDatabaseName" class="col-sm-3 control-label">Database name *</label>
            <div class="col-sm-9">
              <input type="text" class="form-control" id="inputDatabaseName" name="database" value="molgenis" required>
            </div>
          </div>
          <div class="form-group">
            <label for="inputUsername" class="col-sm-3 control-label">Username *</label>
            <div class="col-sm-9">
              <input type="text" class="form-control" id="inputUsername" name="username" required>
            </div>
          </div>
          <div class="form-group">
            <label for="inputPassword" class="col-sm-3 control-label">Password *</label>
            <div class="col-sm-9">
              <input type="password" class="form-control" id="inputPassword" name="password" required>
            </div>
          </div>
          <legend>Elasticsearch configuration</legend>
            <div class="form-group">
                <label for="inputClusterName" class="col-sm-3 control-label">Cluster name *</label>
                <div class="col-sm-9">
                    <input type="text" class="form-control" id="inputClusterName" name="clusterName" value="molgenis" required>
                </div>
            </div>
            <div class="form-group">
                <label for="inputTransportAddresses" class="col-sm-3 control-label">Transport addresses *</label>
                <div class="col-sm-9">
                    <input type="text" class="form-control" id="inputTransportAddresses" name="transportAddresses" value="127.0.0.1:9300" required>
                </div>
            </div>
          <legend>Administrator settings</legend>
            <div class="form-group">
                <label for="inputAdminPassword" class="col-sm-3 control-label">Password *</label>
                <div class="col-sm-9">
                  <input type="password" class="form-control" id="inputAdminPassword" name="adminPassword" required>
                </div>
            </div>
            <div class="form-group">
                <label for="inputAdminEmail" class="col-sm-3 control-label">Email *</label>
                <div class="col-sm-9">
                  <input type="text" class="form-control" id="inputAdminEmail" name="adminEmail" required>
                </div>
            </div>
          <div style="text-align:right">
            <button type="submit" class="btn btn-primary">Create</button>
          </div>
        </form>
    </div>
</body>

</html>
