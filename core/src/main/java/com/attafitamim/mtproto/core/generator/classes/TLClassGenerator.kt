package com.attafitamim.mtproto.core.generator.classes

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.attafitamim.mtproto.core.generator.classes.Constants.BASE_PACKAGE_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.BUFFER_PARAMETER_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.EXCEPTION_PARAMETER_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.FLAGS_PROPERTY_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.GLOBAL_DATA_TYPES_FOLDER_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.METHODS_FOLDER_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.OBJECT_NAME_SPACE_SEPARATOR
import com.attafitamim.mtproto.core.generator.classes.Constants.PACKAGE_SEPARATOR
import com.attafitamim.mtproto.core.generator.classes.Constants.PARSE_METHOD_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.PARSE_RESPONSE_METHOD_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.SERIALIZE_METHOD_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.TL_VECTOR_TYPE_CONSTRUCTOR
import com.attafitamim.mtproto.core.generator.classes.Constants.TL_VECTOR_TYPE_NAME
import com.attafitamim.mtproto.core.generator.classes.Constants.TYPES_FOLDER_NAME
import com.attafitamim.mtproto.core.generator.types.TLObjectSpecs
import com.attafitamim.mtproto.core.generator.types.TLPropertySpecs
import com.attafitamim.mtproto.core.objects.MTMethod
import com.attafitamim.mtproto.core.objects.MTObject
import com.attafitamim.mtproto.core.stream.MTInputStream
import com.attafitamim.mtproto.core.stream.MTOutputStream
import org.gradle.api.GradleException
import java.util.*
import kotlin.math.pow

internal object TLClassGenerator {

    private val localTypes = mapOf(
            "string" to String::class,
            "int" to Int::class,
            "long" to Long::class,
            "true" to Boolean::class,
            "double" to Double::class,
            "bytes" to ByteArray::class
    )

    private val String.asFormattedClassName: String get()  {
        val className = this.substringAfterLast(OBJECT_NAME_SPACE_SEPARATOR)
        return TextUtils.camelToTitleCase(className)
    }

    private val String.asNamespace: String? get() {
        val namespaceName = this.substringBeforeLast(OBJECT_NAME_SPACE_SEPARATOR)
        return if (namespaceName == this) null else namespaceName
    }

    fun generateSuperDataObjectClass(name: String, tlObjectsSpecs: List<TLObjectSpecs>): FileSpec {
        try {
            val className = createDataObjectClassName(name)

            val classTypeBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(KModifier.ABSTRACT)
                    .superclass(MTObject::class)

            tlObjectsSpecs.groupBy(TLObjectSpecs::name).forEach { group ->
                if (group.value.size == 1) {
                    val objectClass = generateDataObjectClass(group.value.first())
                    classTypeBuilder.addType(objectClass)
                } else {
                    group.value.forEach { tlObjectSpecs ->
                        tlObjectSpecs.name = "${tlObjectSpecs.name}_${Integer.toHexString(tlObjectSpecs.constructor)}"
                        val objectClass = generateDataObjectClass(tlObjectSpecs)
                        classTypeBuilder.addType(objectClass)
                    }
                }
            }

            val companionObjectBuilder = TypeSpec.companionObjectBuilder()
                    .addBaseObjectParseFunction(className, tlObjectsSpecs)
                    .build()

            val classTypeSpec = classTypeBuilder.addType(companionObjectBuilder)
                    .build()

            return FileSpec.builder(className.packageName, className.simpleName)
                    .addType(classTypeSpec)
                    .build()


        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to generate Base TL Object
                        ObjectName: $name
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun generateMethodClass(tlObjectSpecs: TLObjectSpecs): FileSpec {
        try {
            val responseClassName = createDataObjectClassName(tlObjectSpecs.superClassName)
            val className = createMethodClassName(tlObjectSpecs.name)
            val classBuilder = TypeSpec.classBuilder(className).addObjectSerializeFunction(tlObjectSpecs)

            if (tlObjectSpecs.superClassName.startsWith(TL_VECTOR_TYPE_NAME, true)) {
                val vectorGenericType = getVectorGenericType(tlObjectSpecs.superClassName)
                val listTypeName = List::class.asTypeName().parameterizedBy(vectorGenericType)
                val superClassName = MTMethod::class.asClassName()
                        .parameterizedBy(listTypeName)

                classBuilder.addMethodVectorParseFunction(tlObjectSpecs.superClassName).superclass(superClassName)
            } else {
                val superClassName = MTMethod::class.asClassName()
                        .parameterizedBy(responseClassName)

                classBuilder.addMethodParseFunction(responseClassName).superclass(superClassName)
            }

            val objectProperties = tlObjectSpecs.propertiesSpecs?.map { tlPropertySpec ->
                createObjectPropertySpec(tlPropertySpec)
            }

            if (!objectProperties.isNullOrEmpty()) {
                classBuilder.addPrimaryConstructor(objectProperties).addModifiers(KModifier.DATA)
            }

            val hashConstant = createConstantPropertySpec(
                    tlObjectSpecs::constructor.name,
                    tlObjectSpecs.constructor
            )

            val hashPropertySpec = PropertySpec.builder(TLObjectSpecs::constructor.name, Int::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%L", hashConstant.name)
                    .build()

            val companionObjectBuilder = TypeSpec.companionObjectBuilder()
                    .addProperty(hashConstant)
                    .build()

            val classSpec = classBuilder.addType(companionObjectBuilder)
                    .addProperty(hashPropertySpec)
                    .addKdoc("Scheme: ${tlObjectSpecs.rawScheme}")
                    .build()

            return FileSpec.builder(className.packageName, className.simpleName)
                    .addType(classSpec)
                    .build()

        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to generate TL Method
                        TLObjectSpecs: $tlObjectSpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun generateDataObjectClass(tlObjectSpecs: TLObjectSpecs): TypeSpec {
        try {
            val superClassName = createDataObjectClassName(tlObjectSpecs.superClassName)
            val className = createDataObjectClassName(tlObjectSpecs.name, tlObjectSpecs.superClassName)

            val classBuilder = TypeSpec.classBuilder(className).superclass(superClassName)

            val objectProperties = tlObjectSpecs.propertiesSpecs?.map { tlPropertySpec ->
                createObjectPropertySpec(tlPropertySpec)
            }

            if (!objectProperties.isNullOrEmpty()) {
                classBuilder.addPrimaryConstructor(objectProperties).addModifiers(KModifier.DATA)
            }

            val hashConstant = createConstantPropertySpec(
                    tlObjectSpecs::constructor.name,
                    tlObjectSpecs.constructor
            )

            val hashPropertySpec = PropertySpec.builder(TLObjectSpecs::constructor.name, Int::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("%L", hashConstant.name)
                    .build()

            val companionObjectBuilder = TypeSpec.companionObjectBuilder()
                    .addProperty(hashConstant)
                    .addObjectParseFunction(tlObjectSpecs, className)
                    .build()

            return classBuilder.addType(companionObjectBuilder)
                    .addProperty(hashPropertySpec)
                    .addObjectSerializeFunction(tlObjectSpecs)
                    .addKdoc("Scheme: ${tlObjectSpecs.rawScheme}")
                    .build()

        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to generate Base TL Object
                        TLObjectSpecs: $tlObjectSpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun TypeSpec.Builder.addPrimaryConstructor(properties: List<PropertySpec>): TypeSpec.Builder {
        try {
            val propertySpecs = properties.map { property ->
                property.toBuilder().initializer(property.name).build()
            }

            val parameters = propertySpecs.map { propertySpec ->
                ParameterSpec.builder(propertySpec.name, propertySpec.type).build()
            }

            val constructor = FunSpec.constructorBuilder()
                    .addParameters(parameters)
                    .build()

            return this.primaryConstructor(constructor)
                    .addProperties(propertySpecs)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to generate Primary constructor for Base TL Object
                        Properties: $properties
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun createObjectPropertySpec(tlPropertySpecs: TLPropertySpecs): PropertySpec {
        try {
            val propertyType = createPropertyClassName(tlPropertySpecs)
            val formattedPropertyName = TextUtils.snakeToCamelCase(tlPropertySpecs.name)
            return PropertySpec.builder(formattedPropertyName, propertyType).build()
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to create property spec for Base TL Object
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun TypeSpec.Builder.addMethodVectorParseFunction(name: String): TypeSpec.Builder = this.apply {
        try {
            if (!name.startsWith(TL_VECTOR_TYPE_NAME, true)) return@apply

            val bufferClassName = MTInputStream::class.asClassName()

            val vectorGenericName = getVectorGenericName(name)
            val itemTypeName = getVectorGenericType(name)
            val isPremitiveType = localTypes.containsKey(vectorGenericName)
            val vectorClassName = List::class.asClassName().parameterizedBy(itemTypeName)

            val functionBuilder = FunSpec.builder(PARSE_RESPONSE_METHOD_NAME)
                    .addParameter(BUFFER_PARAMETER_NAME, bufferClassName)
                    .addParameter(TLObjectSpecs::constructor.name, Int::class)
                    .addParameter(EXCEPTION_PARAMETER_NAME, Boolean::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(vectorClassName)

            val vectorName = "items"
            val integerReadStatement = "$BUFFER_PARAMETER_NAME.readInt(${EXCEPTION_PARAMETER_NAME})"

            val hashValidationStatement = "require(${TLObjectSpecs::constructor.name} == $TL_VECTOR_TYPE_CONSTRUCTOR)"
            functionBuilder.addStatement(hashValidationStatement)

            val vectorSizeName = "${vectorName}${TL_VECTOR_TYPE_NAME}Size"
            val vectorSizeReadingStatement = "val $vectorSizeName = $integerReadStatement"
            functionBuilder.addStatement(vectorSizeReadingStatement)

            val vectorGenericType = getVectorGenericType(name)
            val arrayInitializationStatement = "val $vectorName = ArrayList<$vectorGenericType>()"
            functionBuilder.addStatement(arrayInitializationStatement)

            val vectorLoopStatement = "for (index in 0 until $vectorSizeName)"
            functionBuilder.beginControlFlow(vectorLoopStatement)

            val itemHashName = "item${TextUtils.camelToTitleCase(TLObjectSpecs::constructor.name)}"
            val itemHashInitializationStatement = "val $itemHashName = $integerReadStatement"

            val itemReadStatement = if (isPremitiveType) {
                "$BUFFER_PARAMETER_NAME.read${itemTypeName.simpleName}($EXCEPTION_PARAMETER_NAME)"
            } else {
                functionBuilder.addStatement(itemHashInitializationStatement)
                "${itemTypeName.simpleName}.$PARSE_METHOD_NAME($BUFFER_PARAMETER_NAME, $itemHashName, $EXCEPTION_PARAMETER_NAME)"
            }

            val itemName = "item"
            val itemInitializationStatement = "val $itemName = $itemReadStatement"
            functionBuilder.addStatement(itemInitializationStatement)

            val itemInsertionStatement = "${vectorName}.add($itemName)"
            functionBuilder.addStatement(itemInsertionStatement).endControlFlow()

            val vectorReturnStatement = "return $vectorName"
            functionBuilder.addStatement(vectorReturnStatement)

            val functionSpec = functionBuilder.build()
            addFunction(functionSpec)

        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add method vector parse function
                        Name: $name
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun TypeSpec.Builder.addMethodParseFunction(returnType: ClassName): TypeSpec.Builder = this.apply {
        try {
            val bufferClassName = MTInputStream::class.asClassName()

            val returnStatement = "return ${returnType.simpleName}.$PARSE_METHOD_NAME($BUFFER_PARAMETER_NAME, ${TLObjectSpecs::constructor.name}, $EXCEPTION_PARAMETER_NAME)"

            val functionSpec = FunSpec.builder(PARSE_RESPONSE_METHOD_NAME)
                    .addParameter(BUFFER_PARAMETER_NAME, bufferClassName)
                    .addParameter(TLObjectSpecs::constructor.name, Int::class)
                    .addParameter(EXCEPTION_PARAMETER_NAME, Boolean::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(returnType)
                    .addStatement(returnStatement)
                    .build()

            addFunction(functionSpec)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add method parse function
                        ReturnType: $returnType
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun TypeSpec.Builder.addObjectSerializeFunction(tlObjectSpecs: TLObjectSpecs): TypeSpec.Builder = this.apply {
        try {
            val bufferClassName = MTOutputStream::class.asClassName()

            val functionBuilder = FunSpec.builder(SERIALIZE_METHOD_NAME)
                    .addParameter(BUFFER_PARAMETER_NAME, bufferClassName)
                    .addModifiers(KModifier.OVERRIDE)

            val hashSerializationStatement = "$BUFFER_PARAMETER_NAME.writeInt(${TLObjectSpecs::constructor.name})"
            functionBuilder.addStatement(hashSerializationStatement)

            if (tlObjectSpecs.hasFlags) {
                functionBuilder.addStatement("")
                val flagsInitializationStatement = "var $FLAGS_PROPERTY_NAME = 0"
                functionBuilder.addStatement(flagsInitializationStatement)

                tlObjectSpecs.propertiesSpecs?.forEach { tlPropertySpecs ->
                    functionBuilder.addPropertyFlaggingStatement(tlPropertySpecs)
                }

                val flagsSerializationStatement = "$BUFFER_PARAMETER_NAME.writeInt($FLAGS_PROPERTY_NAME)"
                functionBuilder.addStatement(flagsSerializationStatement)
            }

            tlObjectSpecs.propertiesSpecs?.forEach { tlPropertySpecs ->
                functionBuilder.addPropertySerializingStatement(tlPropertySpecs)
            }

            val functionSpec = functionBuilder.build()
            addFunction(functionSpec)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add serialize function for Input TL Object
                        TLObjectSpecs: $tlObjectSpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun TypeSpec.Builder.addBaseObjectParseFunction(returnType: ClassName, tlObjectsSpecs: List<TLObjectSpecs>): TypeSpec.Builder = this.apply {
        try {
            val bufferClassName = MTInputStream::class.asClassName()

            val hashParameterName = TLObjectSpecs::constructor.name
            val functionBuilder = FunSpec.builder(PARSE_METHOD_NAME)
                    .addParameter(BUFFER_PARAMETER_NAME, bufferClassName)
                    .addParameter(hashParameterName, Int::class)
                    .addParameter(EXCEPTION_PARAMETER_NAME, Boolean::class)

            functionBuilder.beginControlFlow("return when($hashParameterName)")
            tlObjectsSpecs.groupBy(TLObjectSpecs::name).forEach { group ->

                if (group.value.size == 1) {
                    val objectClass = createDataObjectClassName(group.value.first().name, group.value.first().superClassName)
                    functionBuilder.addStatement(
                            "${objectClass.simpleName}.${TLObjectSpecs::constructor.name.toUpperCase()} -> ${objectClass.simpleName}.$PARSE_METHOD_NAME($BUFFER_PARAMETER_NAME, $hashParameterName, $EXCEPTION_PARAMETER_NAME)"
                    )
                } else {
                    group.value.forEach { tlObjectSpecs ->
                        val className = "${tlObjectSpecs.name}_${Integer.toHexString(tlObjectSpecs.constructor)}"
                        val objectClass = createDataObjectClassName(className, tlObjectSpecs.superClassName)
                        functionBuilder.addStatement(
                                "${objectClass.simpleName}.${TLObjectSpecs::constructor.name.toUpperCase()} -> ${objectClass.simpleName}.$PARSE_METHOD_NAME($BUFFER_PARAMETER_NAME, $hashParameterName, $EXCEPTION_PARAMETER_NAME)"
                        )
                    }
                }

            }

            val illegalArgumentExceptionName = IllegalArgumentException::class.simpleName
            val hashToHexStatement = "Integer.toHexString($hashParameterName)"
            val parseErrorStatmenet = "\"\"\"\nCan't parse ${returnType.simpleName} with hash \${$hashToHexStatement}\n\"\"\""
            functionBuilder.addStatement("else -> throw $illegalArgumentExceptionName(\n$parseErrorStatmenet\n)").endControlFlow()
            val functionSpec = functionBuilder.returns(returnType).build()
            addFunction(functionSpec)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add parse function for Base TL Object
                        Return Type: $returnType
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun TypeSpec.Builder.addObjectParseFunction(tlObjectSpecs: TLObjectSpecs, returnType: ClassName): TypeSpec.Builder = this.apply {
        try {
            val bufferClassName = MTInputStream::class.asClassName()
            val functionBuilder = FunSpec.builder(PARSE_METHOD_NAME)
                    .addParameter(BUFFER_PARAMETER_NAME, bufferClassName)
                    .addParameter(TLObjectSpecs::constructor.name, Int::class)
                    .addParameter(EXCEPTION_PARAMETER_NAME, Boolean::class)
                    .addHashValidationMethod()

            if (tlObjectSpecs.hasFlags) {
                functionBuilder.addFlagReadingStatement()
            }

            tlObjectSpecs.propertiesSpecs?.forEach { tlPropertySpecs ->
                functionBuilder.addPropertyParsingStatement(tlPropertySpecs)
            }

            functionBuilder.addObjectReturnStatement(tlObjectSpecs, returnType).returns(returnType)
            val functionSpec = functionBuilder.build()

            addFunction(functionSpec)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add parse function for TL Object
                        TLObjectSpecs: $tlObjectSpecs
                        Return Type: $returnType
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addPropertyFlaggingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        try {
            if (tlPropertySpecs.flag == null) return@apply

            val formattedPropertyName = TextUtils.snakeToCamelCase(tlPropertySpecs.name)

            val primitiveType = localTypes[tlPropertySpecs.type]
            val objectTypeName = primitiveType?.simpleName ?: createDataObjectClassName(tlPropertySpecs.type).simpleName

            val flagCheckStatement = if (objectTypeName == Boolean::class.simpleName) {
                formattedPropertyName
            } else {
                "$formattedPropertyName != null"
            }

            val flagPosition = 2.0.pow(tlPropertySpecs.flag).toInt()
            val booleanFlaggingStatement = "$FLAGS_PROPERTY_NAME = if ($flagCheckStatement) ($FLAGS_PROPERTY_NAME or $flagPosition) else ($FLAGS_PROPERTY_NAME and $flagPosition.inv())"
            addStatement(booleanFlaggingStatement)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add property flagging statement
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addPropertySerializingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        try {
            if (tlPropertySpecs.type.startsWith(TL_VECTOR_TYPE_NAME, true)) addVectorSerializationStatement(tlPropertySpecs)
            else addTypeSerializingStatement(tlPropertySpecs)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add property serializing statement
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addTypeSerializingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        try {
            val formattedPropertyName = TextUtils.snakeToCamelCase(tlPropertySpecs.name)

            val primitiveType = localTypes[tlPropertySpecs.type]
            val objectTypeName = primitiveType?.simpleName ?: createDataObjectClassName(tlPropertySpecs.type).simpleName
            if (objectTypeName == Boolean::class.simpleName) return@apply

            val readStatement = if (primitiveType != null) {
                "$BUFFER_PARAMETER_NAME.write$objectTypeName($formattedPropertyName)"
            } else {
                "$formattedPropertyName.$SERIALIZE_METHOD_NAME($BUFFER_PARAMETER_NAME)"
            }

            if (tlPropertySpecs.flag != null) {
                addStatement("")
                val nullabilityCheckStatement = "$formattedPropertyName != null"
                val flagCheckingStatement = "if ($nullabilityCheckStatement)"
                addStatement(flagCheckingStatement)
            }

            addStatement(readStatement)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add type property serializing statement
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addVectorSerializationStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        try {
            if (!tlPropertySpecs.type.startsWith(TL_VECTOR_TYPE_NAME, true)) return@apply
            addStatement("")

            val formattedPropertyName = TextUtils.snakeToCamelCase(tlPropertySpecs.name)

            if (tlPropertySpecs.flag != null) {
                val nullabilityCheckStatement = "$formattedPropertyName != null"
                val flagCheckingStatement = "if ($nullabilityCheckStatement)"
                beginControlFlow(flagCheckingStatement)
            }

            val vectorHashSerializingStatement = "$BUFFER_PARAMETER_NAME.writeInt($TL_VECTOR_TYPE_CONSTRUCTOR)"
            addStatement(vectorHashSerializingStatement)

            val vectorSizeName = "${formattedPropertyName}.${List<*>::size.name}"
            val vectorSizeSerializingStatement = "$BUFFER_PARAMETER_NAME.writeInt($vectorSizeName)"
            addStatement(vectorSizeSerializingStatement)

            val vectorItemName = "${formattedPropertyName}Item"
            val vectorLoopStatement = "${formattedPropertyName}.forEach { $vectorItemName ->"
            beginControlFlow(vectorLoopStatement)

            val vectorTypeName = getVectorGenericName(tlPropertySpecs.type)
            val isPrimitiveType = localTypes.contains(vectorTypeName)
            val itemTypeName = getVectorGenericType(tlPropertySpecs.type)

            val itemWriteStatement = if (isPrimitiveType) {
                "$BUFFER_PARAMETER_NAME.write${itemTypeName.simpleName}($vectorItemName)"
            } else {
                "$vectorItemName.$SERIALIZE_METHOD_NAME($BUFFER_PARAMETER_NAME)"
            }

            addStatement(itemWriteStatement).endControlFlow()

            if (tlPropertySpecs.flag != null) {
                endControlFlow()
            }

        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add vector property serialization statement
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addPropertyParsingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        try {
            if (tlPropertySpecs.type.startsWith(TL_VECTOR_TYPE_NAME, true)) addVectorParsingStatement(tlPropertySpecs)
            else addTypeParsingStatement(tlPropertySpecs)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add property parsing statement
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addVectorParsingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        try {
            if (!tlPropertySpecs.type.startsWith(TL_VECTOR_TYPE_NAME, true)) return@apply
            addStatement("")

            val formattedPropertyName = TextUtils.snakeToCamelCase(tlPropertySpecs.name)
            val vectorGenericType = getVectorGenericType(tlPropertySpecs.type)
            val arrayInitializationStatement = "val $formattedPropertyName = ArrayList<$vectorGenericType>()"
            addStatement(arrayInitializationStatement)

            if (tlPropertySpecs.flag != null) {
                val flagPosition = 2.0.pow(tlPropertySpecs.flag).toInt()
                val flagBooleanStatement = "($FLAGS_PROPERTY_NAME and $flagPosition) != 0"
                val flagCheckingStatement = "if ($flagBooleanStatement)"
                beginControlFlow(flagCheckingStatement)
            }

            val integerReadStatement = "$BUFFER_PARAMETER_NAME.readInt(${EXCEPTION_PARAMETER_NAME})"
            val vectorHashName = "${formattedPropertyName}${TL_VECTOR_TYPE_NAME}Hash"
            val vectorHashReadingStatement = "val $vectorHashName = $integerReadStatement"
            addStatement(vectorHashReadingStatement)

            val hashValidationStatement = "require($vectorHashName == $TL_VECTOR_TYPE_CONSTRUCTOR)"
            addStatement(hashValidationStatement)

            val vectorSizeName = "${formattedPropertyName}${TL_VECTOR_TYPE_NAME}Size"
            val vectorSizeReadingStatement = "val $vectorSizeName = $integerReadStatement"
            addStatement(vectorSizeReadingStatement)

            val vectorLoopStatement = "for (index in 0 until $vectorSizeName)"
            beginControlFlow(vectorLoopStatement)

            val vectorGenericName = getVectorGenericName(tlPropertySpecs.type)
            val itemPrimitiveType = localTypes[vectorGenericName]
            val itemTypeName = itemPrimitiveType?.simpleName ?: getVectorGenericType(tlPropertySpecs.type).simpleName
            val itemHashName = "${formattedPropertyName}ItemHash"
            val itemHashInitializationStatement = "val $itemHashName = $integerReadStatement"

            val itemReadStatement = if (itemPrimitiveType != null) {
                "$BUFFER_PARAMETER_NAME.read$itemTypeName($EXCEPTION_PARAMETER_NAME)"
            } else {
                addStatement(itemHashInitializationStatement)
                "$itemTypeName.$PARSE_METHOD_NAME($BUFFER_PARAMETER_NAME, $itemHashName, $EXCEPTION_PARAMETER_NAME)"
            }

            val itemName = "${formattedPropertyName}Item"
            val itemInitializationStatement = "val $itemName = $itemReadStatement"
            addStatement(itemInitializationStatement)

            val itemInsertionStatement = "${formattedPropertyName}.add($itemName)"
            addStatement(itemInsertionStatement).endControlFlow()

            if (tlPropertySpecs.flag != null) {
                endControlFlow()
            }

        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add vector property parsing statement
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addTypeParsingStatement(tlPropertySpecs: TLPropertySpecs): FunSpec.Builder = this.apply {
        try {
            val formattedPropertyName = TextUtils.snakeToCamelCase(tlPropertySpecs.name)

            val primitiveType = localTypes[tlPropertySpecs.type]
            val objectTypeName = primitiveType?.simpleName ?: createDataObjectClassName(tlPropertySpecs.type).simpleName
            val objectHashName = "${formattedPropertyName}Hash"

            val readStatement = if (primitiveType != null) {
                "$BUFFER_PARAMETER_NAME.read$objectTypeName($EXCEPTION_PARAMETER_NAME)"
            } else {
                "$objectTypeName.$PARSE_METHOD_NAME($BUFFER_PARAMETER_NAME, $objectHashName, $EXCEPTION_PARAMETER_NAME)"
            }

            if (tlPropertySpecs.flag != null) {
                val flagPosition = 2.0.pow(tlPropertySpecs.flag).toInt()
                val flagBooleanStatement = "($FLAGS_PROPERTY_NAME and $flagPosition) != 0"

                if (objectTypeName == Boolean::class.simpleName) {
                    val propertyDeclarationStatement = "val $formattedPropertyName: $objectTypeName = $flagBooleanStatement"
                    addStatement(propertyDeclarationStatement)
                } else {
                    addStatement("")
                    val propertyDeclarationStatement = "var $formattedPropertyName: $objectTypeName? = null"
                    addStatement(propertyDeclarationStatement)

                    val flagCheckingStatement = "if ($flagBooleanStatement)"
                    beginControlFlow(flagCheckingStatement)

                    if (primitiveType == null) {
                        val hashReadingStatement = "val $objectHashName = $BUFFER_PARAMETER_NAME.readInt($EXCEPTION_PARAMETER_NAME)"
                        addStatement(hashReadingStatement)
                    }

                    val readNullableValueStatement = "$formattedPropertyName = $readStatement"
                    addStatement(readNullableValueStatement).endControlFlow()
                }
            } else {
                if (primitiveType == null) {
                    val hashReadingStatement = "val $objectHashName = $BUFFER_PARAMETER_NAME.readInt($EXCEPTION_PARAMETER_NAME)"
                    addStatement(hashReadingStatement)
                }

                val propertyDeclarationStatement = "val $formattedPropertyName: $objectTypeName = $readStatement"
                addStatement(propertyDeclarationStatement)
            }
        }  catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add type property parsing statement
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addFlagReadingStatement(): FunSpec.Builder = this.apply {
        try {
            val flagReadingStatement = "val $FLAGS_PROPERTY_NAME = $BUFFER_PARAMETER_NAME.readInt($EXCEPTION_PARAMETER_NAME)"
            addStatement(flagReadingStatement)
        }  catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add flag reading statement
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addHashValidationMethod() : FunSpec.Builder = this.apply {
        try {
            val hashParameterName = TLObjectSpecs::constructor.name
            val hashValidationStatement = "require($hashParameterName == ${hashParameterName.toUpperCase()})"
            addStatement(hashValidationStatement)
        }  catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add hash validation method
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun FunSpec.Builder.addObjectReturnStatement(
        tlObjectSpecs: TLObjectSpecs,
        returnType: ClassName
    ) : FunSpec.Builder = this.apply {
        try {
            addStatement("")
            addStatement("return ${returnType.simpleName}(")

            tlObjectSpecs.propertiesSpecs?.forEach { tlPropertySpecs ->
                val formattedPropertyName = TextUtils.snakeToCamelCase(tlPropertySpecs.name)
                addStatement("$formattedPropertyName,")
            }

            addStatement(")")
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to add object return statement
                        TLObjectSpecs: $tlObjectSpecs
                        Return Type: $returnType
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun createConstantPropertySpec(name: String, value: Any): PropertySpec {
        try {
            val constantName = name.toUpperCase(Locale.ROOT)
            return PropertySpec.builder(constantName, value::class)
                    .mutable(false)
                    .addModifiers(KModifier.CONST)
                    .initializer("%L", value)
                    .build()
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to create constant property spec
                        Name: $name
                        Value: $value
                    """.trimIndent()
            )
        }
    }

    fun createPropertyClassName(tlPropertySpecs: TLPropertySpecs): TypeName {
        try {
            val primitiveClass = localTypes[tlPropertySpecs.type]
            var propertyType = primitiveClass?.asTypeName() ?: createTLObjectTypeName(tlPropertySpecs.type)

            if (tlPropertySpecs.type != "true" && tlPropertySpecs.flag != null) {
                propertyType = propertyType.copy(true)
            }

            return propertyType
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to create property class name
                        TLPropertySpecs: $tlPropertySpecs
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun createMethodClassName(name: String): ClassName {
        try {
            val formattedNameSpace = name.asNamespace?.let(TextUtils::camelToTitleCase).orEmpty()
            val nameSpace = name.asNamespace ?: GLOBAL_DATA_TYPES_FOLDER_NAME
            val packageName = StringBuilder(BASE_PACKAGE_NAME)
                    .append(PACKAGE_SEPARATOR)
                    .append(METHODS_FOLDER_NAME)
                    .append(PACKAGE_SEPARATOR)
                    .append(nameSpace)
                    .toString()

            val actualClassName = "${Constants.TYPES_PREFIX}$formattedNameSpace${name.asFormattedClassName}"
            return ClassName(packageName, actualClassName)
        }  catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to create method class name
                        Method Name: $name
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun createTLObjectTypeName(name: String, superClassName: String? = null): TypeName {
        try {
            return if (name.startsWith(TL_VECTOR_TYPE_NAME, true)) createVectorClassName(name)
            else createDataObjectClassName(name, superClassName)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to create TL Object type name
                        Name: $name
                        Super Class Name: $superClassName
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun createVectorClassName(name: String): TypeName {
        try {
            val genericClassName = getVectorGenericType(name)
            return List::class.asClassName().parameterizedBy(genericClassName)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to create vector class name
                        Name: $name
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun getVectorGenericType(name: String): ClassName {
        try {
            val genericTypeName = getVectorGenericName(name)
            val primitiveType = localTypes[genericTypeName]
            return primitiveType?.asClassName() ?: createDataObjectClassName(genericTypeName)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to get vector generic type
                        Name: $name
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

    fun getVectorGenericName(name: String): String {
        return name.substringAfter("<").substringBeforeLast(">")
    }

    fun createDataObjectClassName(name: String, superClassName: String? = null): ClassName {
        try {

            val classNames: List<String>
            val namespace: String
            if (!superClassName.isNullOrBlank()) {
                val formattedNameSpace = superClassName.asNamespace?.let(TextUtils::camelToTitleCase).orEmpty()
                val actualSuperClassName = "${Constants.TYPES_PREFIX}${formattedNameSpace}${superClassName.asFormattedClassName}"

                namespace = superClassName.asNamespace ?: GLOBAL_DATA_TYPES_FOLDER_NAME
                classNames = listOfNotNull(
                        actualSuperClassName,
                        name.asFormattedClassName
                )
            } else {
                val formattedNameSpace = name.asNamespace?.let(TextUtils::camelToTitleCase).orEmpty()
                val actualClassName = "${Constants.TYPES_PREFIX}$formattedNameSpace${name.asFormattedClassName}"

                namespace = name.asNamespace ?: GLOBAL_DATA_TYPES_FOLDER_NAME
                classNames = listOf(actualClassName)
            }

            val packageName = StringBuilder(BASE_PACKAGE_NAME)
                    .append(PACKAGE_SEPARATOR)
                    .append(TYPES_FOLDER_NAME)
                    .append(PACKAGE_SEPARATOR)
                    .append(namespace)
                    .toString()

            return ClassName(packageName, classNames)
        } catch (exception: Exception) {
            throw GradleException(
                    """
                        Failed to create TL Object class name
                        Name: $name
                        Super Class Name: $superClassName
                        Error: ${exception.message}
                    """.trimIndent()
            )
        }
    }

}