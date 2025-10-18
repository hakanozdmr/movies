import Hero from '../hero/Hero'
import { Container, Row, Col, Card } from 'react-bootstrap'
import { useNavigate } from 'react-router-dom'
import { useState, useEffect } from 'react'
import './Home.css'

const Home = ({movies, user}) => {
  const navigate = useNavigate()
  const [movieWatchlistStatus, setMovieWatchlistStatus] = useState({})

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
        Loading movies...
      </div>
    );
  }

  const goToMovieReviews = (movieId) => {
    navigate(`/Reviews/${movieId}`)
  }

  // Check watchlist status for movies (similar to Hero component)
  useEffect(() => {
    if (!user || !movies) return
    
    const checkWatchlistStatus = async () => {
      try {
        const watchlistApi = (await import('../api/authApi')).watchlistApi
        const statusPromises = movies.map(async (movie) => {
          try {
            const response = await watchlistApi.checkInWatchlist(user.userId, movie.imdbId)
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
  }, [user, movies])

  return (
    <div className="home-container">
      {/* Hero Section */}
      <Hero movies={movies} user={user} />
      
      {/* All Movies Section */}
      <div className="all-movies-section">
        <Container>
          <div className="movies-section-header">
            <h2 className="section-title">All Movies</h2>
            <p className="section-subtitle">Discover our complete collection</p>
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