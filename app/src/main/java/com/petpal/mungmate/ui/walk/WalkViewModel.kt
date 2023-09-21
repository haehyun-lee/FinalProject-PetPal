package com.petpal.mungmate.ui.walk

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petpal.mungmate.model.Favorite
import com.petpal.mungmate.model.KakaoSearchResponse
import com.petpal.mungmate.model.PlaceData
import com.petpal.mungmate.model.ReceiveUser
import com.petpal.mungmate.model.Review
import com.petpal.mungmate.model.UserBasicInfoData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WalkViewModel(private val repository: WalkRepository) : ViewModel() {
    val searchResults: MutableLiveData<KakaoSearchResponse> = MutableLiveData()

    val reviewCount: MutableLiveData<Int> = MutableLiveData()
    val latestReviews: MutableLiveData<List<Review>> = MutableLiveData()
    val placeInfo: MutableLiveData<Map<String, Any?>?> = MutableLiveData()
    val isPlaceFavorited = MutableStateFlow<Boolean?>(null)
    private val errorMessage: MutableLiveData<String> = MutableLiveData()
    private val _favoriteCount = MutableStateFlow<Int?>(null)
    val isDataLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val favoriteCount: StateFlow<Int?> = _favoriteCount
    val reviewsForPlace = MutableLiveData<List<Review>>()
    val averageRatingForPlace = MutableLiveData<Float>()
    val userNickname: MutableLiveData<String?> = MutableLiveData()
    val usersOnWalk: MutableLiveData<List<ReceiveUser>> = MutableLiveData()
    val walkMatchingCount: MutableLiveData<Int> = MutableLiveData()
    private val _isUserBlocked = MutableLiveData<Boolean>()
    val isUserBlocked: LiveData<Boolean> get() = _isUserBlocked
    fun searchPlacesByKeyword(latitude: Double, longitude: Double, query: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchPlacesByKeyword(latitude, longitude, query)
                searchResults.postValue(results)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Unknown error")
            }
        }
    }
    fun searchPlacesByKeywordFilter(latitude: Double, longitude: Double, query: String,radius:Int) {
        viewModelScope.launch {
            try {
                val results = repository.searchPlacesByKeywordFilter(latitude, longitude, query,radius)
                searchResults.postValue(results)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun fetchPlaceInfoFromFirestore(placeId: String) {
        viewModelScope.launch {
            isDataLoading.postValue(true)  // 데이터 로딩 시작
            try {
                val placeData = repository.getPlaceInfoFromFirestore(placeId)
                placeInfo.postValue(placeData)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to fetch place info")
            } finally {
                isDataLoading.postValue(false)  // 데이터 로딩 완료
            }
        }
    }

    fun fetchAverageRatingForPlace(placeId: String) {
        viewModelScope.launch {
            try {
                val reviews = repository.fetchAllReviewsForPlace(placeId)
                reviewsForPlace.postValue(reviews)
                val totalRating = reviews.map { it.rating ?: 0f }.sum()
                val avgRating = if (reviews.isNotEmpty()) totalRating / reviews.size else 0f
                averageRatingForPlace.postValue(avgRating)

            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to fetch reviews")
            }
        }
    }

    fun fetchFavoriteCount(placeId: String) {
        viewModelScope.launch {
            repository.observeFavoritesChanges(placeId).collect { count ->
                _favoriteCount.value = count
            }
        }
        viewModelScope.launch {
            try {
                val count = repository.getFavoriteCountSuspend(placeId)
                _favoriteCount.value = count
            } catch (e: Exception) {
                errorMessage.value = e.localizedMessage ?: "Failed to fetch favorite count"
            }
        }
    }

    fun fetchReviewCount(placeId: String) {
        viewModelScope.launch {
            try {
                val count = repository.getReviewCountSuspend(placeId)
                reviewCount.postValue(count)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to fetch review count")
            }
        }
    }
    fun fetchIsPlaceFavoritedByUser(placeId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.isPlaceFavoritedByUser(placeId, userId).collect { isFavorited ->
                    isPlaceFavorited.value = isFavorited
                }
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to check if place is favorited")
            }
        }
    }

    fun fetchLatestReviewsForPlace(placeId: String) {
        viewModelScope.launch {
            try {
                val reviews = repository.fetchLatestReviewsSuspend(placeId)
                latestReviews.postValue(reviews)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to fetch latest reviews")
            }
        }
    }

    fun addPlaceToFavorite(placeData: PlaceData, favorite: Favorite) {
        viewModelScope.launch {
            try {
                repository.addFavorite(placeData, favorite)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to add placeData to favorites")
            }
        }
    }

    fun updateLocationAndOnWalkStatus(userId: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                repository.updateLocationAndOnWalkStatusTrue(userId, latitude, longitude)
                // 필요한 경우 성공 메시지나 상태 업데이트
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to update location")
            }
        }
    }
    fun updateOnWalkStatusFalse(userId: String) {
        viewModelScope.launch {
            try {
                repository.updateOnWalkStatusFalse(userId)
                // 필요한 경우 성공 메시지나 상태 업데이트
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to update location")
            }
        }
    }
    fun removeFavorite(placeId: String, userId: String) {
        viewModelScope.launch {
            try {
                repository.removeUserFavorite(placeId, userId)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to remove favorite")
            }
        }
    }
    fun fetchUserNickname(userId: String) {
        viewModelScope.launch {
            try {
                val nickname = repository.fetchUserNicknameByUid(userId)
                userNickname.postValue(nickname)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to fetch user's nickname")
            }
        }
    }
    fun observeUsersOnWalk() {
        viewModelScope.launch {
            repository.observeUsersOnWalkWithPets().collect { users ->
                usersOnWalk.postValue(users)
            }
        }
    }
    fun fetchMatchingWalkCount(userId: String) {
        viewModelScope.launch {
            try {
                val count = repository.fetchMatchingWalkCount(userId)
                walkMatchingCount.postValue(count)
            } catch (e: Exception) {
                errorMessage.postValue(e.localizedMessage ?: "Failed to fetch matching walk count")
            }
        }
    }
    fun blockUser(userId: String, blockId: String) {
        viewModelScope.launch {
            try {
                repository.updateBlockUser(userId, blockId)
                _isUserBlocked.value = true
            } catch (e: Exception) {
                // 에러 처리
                _isUserBlocked.value = false
            }
        }
    }
}