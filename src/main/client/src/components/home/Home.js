import Hero from '../hero/Hero'
import { Container, Row, Col, Card } from 'react-bootstrap'
import { useNavigate } from 'react-router-dom'
import { useState, useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import './Home.css'

const Home = ({movies, user}) => {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [movieWatchlistStatus, setMovieWatchlistStatus] = useState({})
  const moviesRef = useRef(movies)
  const userRef = useRef(user)

  // Update refs when props change
  moviesRef.current = movies
  userRef.current = user

  // Check watchlist status for movies (similar to Hero component)
  useEffect(() => {
    const checkWatchlistStatus = async () => {
      if (!userRef.current || !moviesRef.current || !Array.isArray(moviesRef.current)) return
      
      try {
        const watchlistApi = (await import('../api/authApi')).watchlistApi
        const statusPromises = moviesRef.current.map(async (movie) => {
          try {
            const response = await watchlistApi.checkInWatchlist(userRef.current.userId, movie.imdbId)
            const isInWatchlist = Boolean(response?.inWatchlist)
            return { movieId: movie.imdbId, isInWatchlist }
          } catch (error) {
            console.error('Error checking watchlist status:', error)
            return { movieId: movie.imdbId, isInWatchlist: false }
          }
        })

        const results = await Promise.all(statusPromises)
        const statusMap = {}
        results.forEach(result => {
          statusMap[result.movieId] = result.isInWatchlist
        })
        setMovieWatchlistStatus(statusMap)
      } catch (error) {
        console.error('Error updating watchlist status:', error)
      }
    }

    checkWatchlistStatus()
  }, [user?.userId, movies?.length]) // Only depend on user ID and movies length, not the entire objects

  const goToMovieReviews = (movieId) => {
    navigate(`/Reviews/${movieId}`)
  }

  // Don't render until movies are loaded
  if (!movies || movies.length === 0) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh', 
        color: 'white',
        fontSize: '18px'
      }}>
{t('home.loadingMovies')}
      </div>
    );
  }

  return (
    <div className="home-container">
      {/* Hero Section */}
      <Hero movies={movies} user={user} />
      
      {/* All Movies Section */}
      <div className="all-movies-section">
        <Container>
          <div className="movies-section-header">
            <h2 className="section-title">{t('home.allMovies')}</h2>
            <p className="section-subtitle">{t('home.discoverCollection')}</p>
          </div>
          
          <Row className="movies-grid">
            {movies.map((movie, index) => (
              <Col key={movie.imdbId || index} xs={6} sm={4} md={3} lg={2} xl={2} className="movie-col">
                <Card className="movie-card" onClick={() => goToMovieReviews(movie.imdbId)}>
                  <div className="movie-poster-container">
                    <Card.Img 
                      variant="top" 
                      src={movie.poster} 
                      alt={movie.title}
                      className="movie-poster-img"
                    />
                    <div className="movie-overlay">
                      <div className="movie-info">
                        <h5 className="movie-title">{movie.title}</h5>
                        {movie.imdbRating && (
                          <div className="movie-rating">
                            <span className="rating-label">IMDB</span>
                            <span className="rating-value">{movie.imdbRating}</span>
                          </div>
                        )}
                        {movie.genres && movie.genres.length > 0 && (
                          <div className="movie-genres">
                            {movie.genres.slice(0, 2).map((genre, idx) => (
                              <span key={idx} className="genre-tag">{genre}</span>
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </div>
    </div>
  )
}

export default Home