function fn() {
  var env = karate.env || 'local';

  var config = {
    env: env,
    baseUrl: karate.properties['karate.baseUrl'] || 'http://localhost:8082',
    connectTimeout: 10000,
    readTimeout: 10000,
    defaultPassword: karate.properties['karate.defaultPassword'] || 'Test@123456'
  };

  if (env == 'ci') {
    config.baseUrl = karate.properties['karate.baseUrl'] || 'http://localhost:8082';
  }

  if (env == 'staging') {
    config.baseUrl = karate.properties['karate.baseUrl'] || 'http://localhost:8082';
  }

  karate.configure('connectTimeout', config.connectTimeout);
  karate.configure('readTimeout', config.readTimeout);

  return config;
}
