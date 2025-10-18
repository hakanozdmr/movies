import "./App.css";
import "./i18n";
import api from "./api/axiosConfig";
import { useState, useEffect } from "react";
import { Routes, Route } from "react-router-dom";
import Layout from "./components/Layout";
import Home from "./components/home/Home";
import Header from "./components/header/Header";
import Trailer from "./components/trailer/Trailer";
import Reviews from "./components/reviews/Reviews";
import NotFound from "./components/notFound/NotFound";
import Login from "./components/auth/Login";
import Register from "./components/auth/Register";
import Watchlist from "./components/watchlist/Watchlist";

function App() {
  const [movies, setMovies] = useState();
  const [movie, setMovie] = useState();
  const [reviews, setReviews] = useState([]);
  const [user, setUser] = useState(null);

  const getMovies = async () => {
    try {
      const response = await api.get("/movies");

      setMovies(response.data);
    } catch (err) {
      console.log(err);
    }
  };

  const getMovieData = async (movieId) => {
    try {
      const response = await api.post(`/movies/searchAndSort`, {
            movie: {
              imdbId: movieId,
            },
      });

      const singleMovie = response.data[0];
      console.log(singleMovie);
      setMovie(singleMovie);

      setReviews(singleMovie.reviewIds);
    } catch (error) {
      console.error(error);
    }
  };

  useEffect(() => {
    getMovies();
    // Check for user on app load
    const userData = localStorage.getItem('user');
    if (userData) {
      const parsedUser = JSON.parse(userData);
      if (parsedUser.isLoggedIn) {
        setUser(parsedUser);
      }
    }
  }, []);

  // Function to update user state from child components
  const updateUser = (userData) => {
    setUser(userData);
  };

  return (
    <div className="App">
      <Header user={user} updateUser={updateUser} />
      <Routes>
        <Route path="/" element={<Layout />}>
          <Route path="/" element={<Home movies={movies} user={user} />}></Route>
          <Route path="/Trailer/:ytTrailerId" element={<Trailer />}></Route>
          <Route
            path="/Reviews/:movieId"
            element={
              <Reviews
                getMovieData={getMovieData}
                movie={movie}
                reviews={reviews}
                setReviews={setReviews}
                user={user}
              />
            }
          ></Route>
          <Route path="/login" element={<Login updateUser={updateUser} />}></Route>
          <Route path="/register" element={<Register updateUser={updateUser} />}></Route>
          <Route path="/watchlist" element={<Watchlist />}></Route>
          <Route path="*" element={<NotFound />}></Route>
        </Route>
      </Routes>
    </div>
  );
}

export default App;
