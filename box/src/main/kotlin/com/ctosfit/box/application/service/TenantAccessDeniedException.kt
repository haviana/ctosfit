package com.ctosfit.box.application.service

class TenantAccessDeniedException : RuntimeException("The authenticated user cannot access the selected box")
