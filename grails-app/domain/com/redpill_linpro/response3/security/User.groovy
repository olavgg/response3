package com.redpill_linpro.response3.security

class User {

    static searchable = [
        only: ['id','username', 'name', 'email']
    ]

	transient springSecurityService

	String username
	String password
    String salt = Salt.getSalt()
	boolean enabled = true
	boolean accountExpired = false
	boolean accountLocked = false
	boolean passwordExpired = false
    
    String name
    String firstname
    String lastname
    String telephone
    String email

	static constraints = {
		username blank: false, unique: true, size:3..32, matches:"^[a-zA-Z0-9]+"
        name blank:true, nullable:true
        firstname blank:true, nullable:true
        lastname blank:true, nullable:true
		password blank: false, size: 10..255
        email blank: false
        salt blank: false, maxSize: 64
        telephone nullable:true, blank:true
	}

	static mapping = {
        table 'response3_user'
		password column: '`password`', length: 64
        id generator: 'sequence', params: [sequence: 'person_seq']
        cache usage:'read-write'
        version false
        
        id index:'person_id_idx'
        name index:'person_name_idx'
        email index:'person_email_idx'
        enabled index:'person_enabled_idx'
	}

	Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set
	}

	def beforeInsert() {
		encodePassword()
        setFullName()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
        if (isDirty('firstname') || isDirty('lastname')) {
            setFullName()
        }
	}

	protected void encodePassword() {
		password = springSecurityService.encodePassword(password, salt)
	}
    
    private void setFullName(){
        this.name = (this.firstname?:"") + " " + (this.lastname?:"") 
    }
    
    public boolean isAdmin(){
        def roles = getAuthorities()
        for(role in roles){
            if(role.authority.equals("ROLE_ADMIN")){
                return true
            }
        }
        return false
    }
}
