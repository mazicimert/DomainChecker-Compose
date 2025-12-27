package com.mehmetmertmazici.domaincheckercompose.data

import android.annotation.SuppressLint
import android.content.Context
import com.mehmetmertmazici.domaincheckercompose.data.repository.AuthRepository
import com.mehmetmertmazici.domaincheckercompose.data.repository.CartRepository
import com.mehmetmertmazici.domaincheckercompose.data.repository.InfoRepository
import com.mehmetmertmazici.domaincheckercompose.data.repository.LocationRepository
import com.mehmetmertmazici.domaincheckercompose.data.repository.UserRepository

/**
 * Basit Service Locator pattern.
 * Tüm repository ve manager'lara tek noktadan erişim sağlar.
 *
 * Kullanım:
 * 1. Application veya MainActivity'de initialize et:
 *    ServiceLocator.initialize(context)
 *
 * 2. İstediğin yerde erişim:
 *    val authRepo = ServiceLocator.authRepository
 */
@SuppressLint("StaticFieldLeak") 
object ServiceLocator {

    private var _context: Context? = null
    private val context: Context
        get() = _context ?: throw IllegalStateException(
            "ServiceLocator must be initialized. Call ServiceLocator.initialize(context) first."
        )

    // Lazy initialization ile singleton instance'lar
    private var _sessionManager: SessionManager? = null
    private var _authRepository: AuthRepository? = null
    private var _userRepository: UserRepository? = null
    private var _cartRepository: CartRepository? = null
    private var _infoRepository: InfoRepository? = null
    private var _locationRepository: LocationRepository? = null


    // ============================================
    // INITIALIZATION
    // ============================================

    fun initialize(context: Context) {
        _context = context.applicationContext
    }

    // ============================================
    // SESSION MANAGER
    // ============================================

    val sessionManager: SessionManager
        get() {
            if (_sessionManager == null) {
                _sessionManager = SessionManager(context)
            }
            return _sessionManager!!
        }

    // ============================================
    // REPOSITORIES
    // ============================================

    val authRepository: AuthRepository
        get() {
            if (_authRepository == null) {
                _authRepository = AuthRepository(sessionManager)
            }
            return _authRepository!!
        }

    val userRepository: UserRepository
        get() {
            if (_userRepository == null) {
                _userRepository = UserRepository(sessionManager)
            }
            return _userRepository!!
        }

    val cartRepository: CartRepository
        get() {
            if (_cartRepository == null) {
                _cartRepository = CartRepository()
            }
            return _cartRepository!!
        }

    val locationRepository: LocationRepository
        get() {
            if (_locationRepository == null) {
                _locationRepository = LocationRepository(context)
            }
            return _locationRepository!!
        }

    val infoRepository: InfoRepository
        get() {
            if (_infoRepository == null) {
                _infoRepository = InfoRepository()
            }
            return _infoRepository!!
        }

    // ============================================
    // RESET (Test veya logout için)
    // ============================================

    fun reset() {
        _sessionManager = null
        _authRepository = null
        _userRepository = null
        _cartRepository = null
        _infoRepository = null
        _locationRepository = null
    }
}