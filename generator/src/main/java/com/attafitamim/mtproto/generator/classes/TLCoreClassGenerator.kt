package com.attafitamim.mtproto.generator.classes

import com.squareup.kotlinpoet.*
import com.attafitamim.mtproto.generator.types.TLObjectSpecs

object TLCoreClassGenerator {

    fun generateTlBufferInterface(): FileSpec {
        val className = createCoreObjectClassName(Constants.DATA_BUFFER_INTERFACE_NAME)

        val intParameter = ParameterSpec.builder("int", Int::class).build()
        val longParameter = ParameterSpec.builder("long", Long::class).build()
        val booleanParameter = ParameterSpec.builder("boolean", Boolean::class).build()
        val byteArrayParameter = ParameterSpec.builder("bytes", ByteArray::class).build()
        val offsetParameter = ParameterSpec.builder("offset", Int::class).build()
        val countParameter = ParameterSpec.builder("count", Int::class).build()
        val byteParameter = ParameterSpec.builder("byte", Byte::class).build()
        val stringParameter = ParameterSpec.builder("string", String::class).build()
        val doubleParameter = ParameterSpec.builder("double", Double::class).build()
        val bufferParameter = ParameterSpec.builder(Constants.BUFFER_PARAMETER_NAME, className).build()
        val exceptionParameter = ParameterSpec.builder(Constants.EXCEPTION_PARAMETER_NAME, Boolean::class).build()

        val interfaceTypeBuilder = TypeSpec.interfaceBuilder(className)
                .addAbstractMethod(Constants.BUFFER_WRITE_INT_FUNCTION_NAME, listOf(intParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_LONG_FUNCTION_NAME, listOf(longParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BOOLEAN_FUNCTION_NAME, listOf(booleanParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BYTES_FUNCTION_NAME, listOf(byteArrayParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BYTES_FUNCTION_NAME, listOf(byteArrayParameter, offsetParameter, countParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BYTE_FUNCTION_NAME, listOf(intParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BYTE_FUNCTION_NAME, listOf(byteParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_STRING_FUNCTION_NAME, listOf(stringParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BYTE_ARRAY_FUNCTION_NAME, listOf(byteArrayParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BYTE_ARRAY_FUNCTION_NAME, listOf(byteArrayParameter, offsetParameter, countParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_DOUBLE_FUNCTION_NAME, listOf(doubleParameter))
                .addAbstractMethod(Constants.BUFFER_WRITE_BYTE_BUFFER_FUNCTION_NAME, listOf(bufferParameter))
                .addAbstractMethod(Constants.BUFFER_READ_INT_FUNCTION_NAME, listOf(exceptionParameter), Int::class.asClassName())
                .addAbstractMethod(Constants.BUFFER_READ_LONG_FUNCTION_NAME, listOf(exceptionParameter), Long::class.asClassName())
                .addAbstractMethod(Constants.BUFFER_READ_BOOLEAN_FUNCTION_NAME, listOf(exceptionParameter), Boolean::class.asClassName())
                .addAbstractMethod(Constants.BUFFER_READ_BYTES_FUNCTION_NAME, listOf(byteArrayParameter, exceptionParameter))
                .addAbstractMethod(Constants.BUFFER_READ_DATA_FUNCTION_NAME, listOf(countParameter, exceptionParameter), ByteArray::class.asClassName())
                .addAbstractMethod(Constants.BUFFER_READ_STRING_FUNCTION_NAME, listOf(exceptionParameter), String::class.asClassName())
                .addAbstractMethod(Constants.BUFFER_READ_BYTE_ARRAY_FUNCTION_NAME, listOf(exceptionParameter), ByteArray::class.asClassName())
                .addAbstractMethod(Constants.BUFFER_READ_DOUBLE_FUNCTION_NAME, listOf(exceptionParameter), Double::class.asClassName())
                .addAbstractMethod(Constants.BUFFER_READ_BYTE_BUFFER_FUNCTION_NAME, listOf(exceptionParameter), className)

        val interfaceTypeSpec = interfaceTypeBuilder.build()
        return FileSpec.builder(className.packageName, className.simpleName)
                .addType(interfaceTypeSpec)
                .build()
    }

    fun generateTLBaseObject(): FileSpec {
        val className = createCoreObjectClassName(Constants.TL_BASE_OBJECT_NAME)

        val hashPropertySpec = PropertySpec.builder(TLObjectSpecs::constructor.name, Int::class)
                .addModifiers(KModifier.ABSTRACT)
                .build()

        val bufferClassName = createCoreObjectClassName(Constants.DATA_BUFFER_INTERFACE_NAME)
        val bufferParameter = ParameterSpec.builder(Constants.BUFFER_PARAMETER_NAME, bufferClassName).build()

        val classTypeSpec = TypeSpec.classBuilder(className)
                .addModifiers(KModifier.ABSTRACT)
                .addProperty(hashPropertySpec)
                .addAbstractMethod(Constants.SERIALIZE_METHOD_NAME, listOf(bufferParameter))
                .build()

        return FileSpec.builder(className.packageName, className.simpleName)
                .addType(classTypeSpec)
                .build()
    }

    fun generateTLBaseMethod(): FileSpec {
        val bufferClassName = createCoreObjectClassName(Constants.DATA_BUFFER_INTERFACE_NAME)
        val superClassName = createCoreObjectClassName(Constants.TL_BASE_OBJECT_NAME)
        val className = createCoreObjectClassName(Constants.TL_BASE_METHOD_NAME)

        val typeVariable = TypeVariableName.invoke("RESPONSE")
        val bufferParameter = ParameterSpec.builder(Constants.BUFFER_PARAMETER_NAME, bufferClassName).build()
        val hashParameter = ParameterSpec.builder(TLObjectSpecs::constructor.name, Int::class).build()
        val exceptionParameter = ParameterSpec.builder(Constants.EXCEPTION_PARAMETER_NAME, Boolean::class).build()

        val classTypeSpec = TypeSpec.classBuilder(className)
                .addTypeVariable(typeVariable)
                .addModifiers(KModifier.ABSTRACT)
                .superclass(superClassName)
                .addAbstractMethod(
                        Constants.PARSE_RESPONSE_METHOD_NAME,
                        listOf(
                            bufferParameter,
                            hashParameter,
                            exceptionParameter
                        ),
                        typeVariable
                )
                .build()

        return FileSpec.builder(className.packageName, className.simpleName)
                .addType(classTypeSpec)
                .build()
    }

    fun TypeSpec.Builder.addAbstractMethod(
            name: String,
            parameters: List<ParameterSpec>? = null,
            returnType: TypeName? = null
    ): TypeSpec.Builder {
        val functionBuilder = FunSpec.builder(name).addModifiers(KModifier.ABSTRACT)

        if (!parameters.isNullOrEmpty()) {
            functionBuilder.addParameters(parameters)
        }

        if (returnType != null) functionBuilder.returns(returnType)
        val functionSpec = functionBuilder.build()
        return addFunction(functionSpec)
    }

    fun createCoreObjectClassName(name: String): ClassName {
        val packageName = StringBuilder(Constants.BASE_PACKAGE_NAME)
                .append(Constants.PACKAGE_SEPARATOR)
                .append(Constants.CORE_FOLDER_NAME)
                .toString()

        return ClassName(packageName, name)
    }

}