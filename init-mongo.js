// MongoDB initialization script
db = db.getSiblingDB('moviesdb');

// Create user for the application
db.createUser({
  user: 'movieuser',
  pwd: 'moviepass',
  roles: [
    {
      role: 'readWrite',
      db: 'moviesdb'
    }
  ]
});

// Create collections
db.createCollection('movies');
db.createCollection('reviews');

print('Database initialized successfully');

